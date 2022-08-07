package com.diguage.marshal.hession;

import com.caucho.hessian.io.*;

/**
 * 自定义 SerializerFactory
 *
 * @author D瓜哥 · https://www.diguage.com
 */
public class GuageSerializerFactory extends AbstractSerializerFactory {
    @Override
    public Serializer getSerializer(Class cl) throws HessianProtocolException {
        return new StringValueSerializer();
    }

    @Override
    public Deserializer getDeserializer(Class cl) throws HessianProtocolException {
        return new StringValueDeserializer(cl);
    }
}
