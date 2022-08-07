package com.diguage;

import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.Serializer;

import java.io.IOException;

/**
 * 客户序自定义列化
 *
 * @author D瓜哥 · https://www.diguage.com
 */
public class CustomerHessianSerializer implements Serializer {
    @Override
    public void writeObject(Object obj, AbstractHessianOutput out) throws IOException {
        out.writeString("123");
    }
}
