package io.opentelemetry.javaagent.instrumentation.springwebmvc;

import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import run.mone.common.Result;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.implementsInterface;
import static io.opentelemetry.javaagent.extension.matcher.ClassLoaderMatcher.hasClassesNamed;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;

@SuppressWarnings("SystemOut")
public class HandlerMethodReturnValueInstrumentation implements TypeInstrumentation {

    @Override
    public ElementMatcher<ClassLoader> classLoaderOptimization() {
        return hasClassesNamed("org.springframework.web.method.support.HandlerMethodReturnValueHandler");
    }

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return implementsInterface(named("org.springframework.web.method.support.HandlerMethodReturnValueHandler"));
    }

    @Override
    public void transform(TypeTransformer transformer) {
        transformer.applyAdviceToMethod(
                isMethod()
                        .and(isPublic())
                        .and(named("handleReturnValue")),
                HandlerMethodReturnValueInstrumentation.class.getName() + "$HandleReturnValueAdvice");
    }

    @SuppressWarnings("unused")
    public static class HandleReturnValueAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void nameResourceAndStartSpan(
                @Advice.Argument(0) Object returnValue,
                @Advice.Argument(3) NativeWebRequest request) {
            if(returnValue != null && returnValue instanceof Result){
                Result result = (Result)returnValue;
                if(request instanceof ServletWebRequest){
                    ServletWebRequest servletRquest = (ServletWebRequest)request;
                    servletRquest.getResponse().addHeader("X-BUSSINESS-CODE",String.valueOf(result.getCode()));
                    servletRquest.getResponse().addHeader("X-BUSSINESS-MESSAGE",result.getMessage());
                }
            }
        }

    }
}
