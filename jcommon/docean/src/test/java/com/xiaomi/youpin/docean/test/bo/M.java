package com.xiaomi.youpin.docean.test.bo;

import io.netty.util.Recycler;
import lombok.Data;

/**
 * @author goodjava@qq.com
 * @date 2022/10/20 14:01
 */
@Data
public class M {

    private int id;

    private String name;

    private Recycler.Handle<M> handle;
}
