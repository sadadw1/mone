package com.xiaomi.mone.file;

import com.google.common.collect.Lists;
import com.xiaomi.mone.file.common.FileInfo;
import com.xiaomi.mone.file.common.FileInfoCache;
import com.xiaomi.mone.file.common.FileUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author goodjava@qq.com
 */
@Slf4j
public class LogFile2 implements ILogFile {

    @Getter
    private String file;

    @Getter
    private Object fileKey;

    @Getter
    private MoneRandomAccessFile raf;

    @Setter
    private ReadListener listener;

    @Setter
    private volatile boolean stop;

    @Setter
    private volatile boolean reOpen;

    @Setter
    private volatile boolean reFresh;

    private volatile boolean exceptionFinish;

    @Getter
    private int beforePointerHashCode;

    @Setter
    private volatile long pointer;

    //行号
    private long lineNumber;

    //每次读取时文件的最大偏移量
    private long maxPointer;

    private String md5;

    public LogFile2() {

    }

    public LogFile2(String file, ReadListener listener) {
        this.file = file;
        File f = new File(this.file);
        this.fileKey = FileUtils.fileKey(f);
        this.md5 = md5(file);
        this.listener = listener;
        this.pointer = readPointer();
    }

    public LogFile2(String file) {
        this.file = file;
        File f = new File(this.file);
        this.fileKey = FileUtils.fileKey(f);
        this.md5 = md5(file);
        this.pointer = readPointer();
    }

    public LogFile2(String file, long pointer, long lineNumber) {
        this.file = file;
        File f = new File(this.file);
        this.fileKey = FileUtils.fileKey(f);
        this.md5 = md5(file);
        this.pointer = pointer;
        this.lineNumber = lineNumber;
    }


    public LogFile2(String file, ReadListener listener, long pointer, long lineNumber) {
        this.file = file;
        this.md5 = md5(file);
        this.listener = listener;
        this.pointer = pointer;
        this.lineNumber = lineNumber;
    }

    private void open() throws FileNotFoundException {
        try {
            //4kb
            this.raf = new MoneRandomAccessFile(file, "r", 1024 * 4);
            reOpen = false;
            reFresh = false;
        } catch (FileNotFoundException e) {
            log.error("open file FileNotFoundException", e);
            throw e;
        } catch (IOException e) {
            log.error("open file IOException", e);
        }
    }

    @Override
    public void readLine() throws IOException {
        while (true) {
            try {
                open();
                //兼容文件切换时，缓存的pointer
                try {
                    log.info("open file:{},pointer:{},lineNumber:{},", file, this.pointer, this.lineNumber);
                    if (pointer > raf.length()) {
                        pointer = 0;
                        lineNumber = 0;
                    }
                } catch (Exception e) {
                    log.error("file.length() IOException, file:{}", this.file, e);
                }
                log.info("rel open file:{},pointer:{},lineNumber:{}", file, this.pointer, this.lineNumber);
                raf.seek(pointer);

                while (true) {
                    listener.setPointer(this);
                    if (this.pointer == -1) {
                        pointer = 0;
                        this.lineNumber = 0;
                        log.info("empty break");
                        break;
                    }
                    String line = raf.getNextLine();
                    if (null != line && lineNumber == 0 && pointer == 0) {
                        String hashLine = line.length() > 100 ? line.substring(0, 100) : line;
                        beforePointerHashCode = hashLine.hashCode();
                    }
                    //大行文件先临时截断
                    line = lineCutOff(line);

                    if (reFresh) {
                        break;
                    }

                    if (reOpen) {
                        pointer = 0;
                        lineNumber = 0;
                        break;
                    }

                    if (stop) {
                        break;
                    }

                    //文件内容被切割，重头开始采集内容
                    if (contentHasCutting(line)) {
                        reOpen = true;
                        pointer = 0;
                        lineNumber = 0;
                        log.warn("file:{} content have been cut, goto reOpen file", file);
                        break;
                    }

                    if (listener.isBreak(line)) {
                        log.info("isBreak:{},pointer:{},lineNumber:{},fileKey:{}", this.file, this.pointer, this.lineNumber, this.fileKey);
                        stop = true;
                        break;
                    }

                    if (exceptionFinish) {
                        log.info("file:{} exceptionFinish", file);
                        break;
                    }

                    if (listener.isContinue(line)) {
                        continue;
                    }

                    try {
                        pointer = raf.getFilePointer();
                        maxPointer = raf.length();
                    } catch (IOException e) {
                        log.error("file.length() IOException, file:{}", this.file, e);
                    }

                    ReadResult readResult = new ReadResult();
                    readResult.setLines(Lists.newArrayList(line));
                    readResult.setPointer(pointer);
                    readResult.setFileMaxPointer(maxPointer);
                    readResult.setFilePathName(file);
                    readResult.setLineNumber(++lineNumber);
                    ReadEvent event = new ReadEvent(readResult);
                    listener.setReadTime();

                    listener.onEvent(event);
                    if (pointer % 100000 == 0 || pointer == 1) {
                        log.info("file readResult:{}", readResult);
                    }
                }
                raf.close();
                if (stop) {
                    log.info("stop:{},pointer:{},lineNumber:{},fileKey:{}", this.file, this.pointer, this.lineNumber, this.fileKey);
                    FileInfoCache.ins().put(this.fileKey.toString(), FileInfo.builder().pointer(this.pointer).fileName(this.file).build());
                    break;
                }
            } catch (Exception e) {
                if (raf != null) {
                    raf.close();
                }
                log.error("readLine error", e);
                if (e instanceof FileNotFoundException) {
                    throw e;
                }
            }
        }
        log.info("read file:{},finished,,pointer:{},lineNumber:{},fileKey:{}", file, this.pointer, this.lineNumber, this.fileKey);
    }

    @Override
    public void initLogFile(String file, ReadListener listener, long pointer, long lineNumber) {
        this.file = file;
        this.md5 = md5(file);
        this.listener = listener;
        this.pointer = pointer;
        this.lineNumber = lineNumber;
    }

    @Override
    public void setExceptionFinish() {
        exceptionFinish = true;
    }

    @Override
    public boolean getExceptionFinish() {
        return exceptionFinish;
    }

    private String lineCutOff(String line) {
        if (null != line) {
            //todo 大行文件先临时截断
            if (line.length() > LINE_MAX_LENGTH) {
                line = line.substring(0, LINE_MAX_LENGTH);
            }
        }

        return line;
    }

    private boolean contentHasCutting(String line) throws IOException {
        if (null != line) {
            return false;
        }

        long currentFileMaxLength;
        try {
            currentFileMaxLength = raf.length();
            if (currentFileMaxLength == 0L) {
                raf.getFD().sync();
                TimeUnit.MILLISECONDS.sleep(30);
                currentFileMaxLength = raf.length();
            }
        } catch (IOException e) {
            log.error("get fileMaxPointer IOException", e);
            return false;
        } catch (InterruptedException e) {
            log.error("get fileMaxPointer InterruptedException", e);
            return false;
        }

        return false;
    }

    public void saveProgress() {
        if (!stop) {
            FileInfoCache.ins().put(this.fileKey.toString(), FileInfo.builder().pointer(this.pointer).fileName(this.file).build());
        }
    }

    public void shutdown() {
        try {
            this.stop = true;
            FileInfoCache.ins().put(this.fileKey.toString(), FileInfo.builder().pointer(this.pointer).fileName(this.file).build());
        } catch (Throwable ex) {
            log.error(ex.getMessage());
        }
    }


    public long readPointer() {
        try {
            FileInfo fi = FileInfoCache.ins().get(this.fileKey.toString());
            log.info("readPointer:{},file:{},fileKey:{}", fi, this.file, this.fileKey);
            if (null != fi && Objects.equals(this.file, fi.getFileName())) {
                return fi.getPointer();
            }
        } catch (Throwable e) {
            log.error("readPointer error,file:{},fileKey:{}", file, fileKey, e);
        }
        log.warn("readPointer from 0,file:{},fileKey:{}", file, fileKey);
        return 0;
    }


    @SneakyThrows
    public String md5(String msg) {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(msg.getBytes());
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder(2 * digest.length);
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString().toUpperCase();
    }


}
