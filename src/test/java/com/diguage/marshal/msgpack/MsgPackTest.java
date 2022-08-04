package com.diguage.marshal.msgpack;

import com.caucho.hessian.io.Hessian2Output;
import com.diguage.User;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.msgpack.MessagePack;
import org.msgpack.template.BigDecimalTemplate;
import org.msgpack.template.DateTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MsgPackTest {

    @Test
    public void test() throws Throwable {
        BigDecimal money = new BigDecimal("12.3456").setScale(2, BigDecimal.ROUND_HALF_UP);
        User user = new User(1, "diguage", new Date(), money);
        objectTo(user);
    }

    /**
     * 对象序列化
     *
     * @author D瓜哥 · https://www.diguage.com/
     */
    public void objectTo(Object value) throws Throwable {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        MessagePack pack = new MessagePack();
        pack.register(Date.class, DateTemplate.getInstance());
        pack.register(BigDecimal.class, BigDecimalTemplate.getInstance());
        pack.register(User.class);

        byte[] result = pack.write(value);

        System.out.println("\n== Object: " + value.getClass().getName() + "  ==");
        if (value instanceof Collection<?> && !((Collection<?>) value).isEmpty()) {
            Optional<?> ele = ((Collection<?>) value).stream().findFirst();
            System.out.println("== Generic: " + ele.get().getClass().getName() + "  ==");
        }
        if (value instanceof Map && !((Map) value).isEmpty()) {
            Optional<? extends Map.Entry<?, ?>> optional =
                    ((Map<?, ?>) value).entrySet().stream().findFirst();
            Map.Entry<?, ?> entry = optional.get();
            Object key = entry.getKey();
            Object val = entry.getValue();
            System.out.println("== Key Object: " + key.getClass().getName() + "  ==");
            System.out.println("== Val Object: " + val.getClass().getName() + "  ==");
        }
        System.out.println(toJson(value));
        System.out.println("== byte array: hessian result ==");
        printBytes(result);
    }

    /**
     * 打印单个字节
     *
     * @author D瓜哥 · https://www.diguage.com/
     */
    private String toJson(Object value) {
        // 需要添加 com.fasterxml.jackson.core:jackson-databind 依赖
        ObjectMapper mapper = new ObjectMapper();
        // 序列化字段
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    public void testBigDecimal() throws IOException {
        bigDecimalTo(BigDecimal.ONE);
        bigDecimalTo(new BigDecimal("12.3456"));
    }

    private void bigDecimalTo(BigDecimal value) throws IOException {
        MessagePack pack = new MessagePack();
        System.out.println("\n\n== BigDecimal: " + value + " ==");
        byte[] write = pack.write(value);
        printBytes(write);
    }

    @Test
    public void testInt() throws IOException {
//        for (int i = -256; i < 256; i++) {
//            printBytes(intTo(i));
//        }
        printBytes(intTo(-129));
        printBytes(intTo(-128));
        printBytes(intTo(-33));
        printBytes(intTo(-32));
        printBytes(intTo(127));
        printBytes(intTo(128));
        printBytes(intTo(255));
        printBytes(intTo(256));
        printBytes(intTo(1024));
        printBytes(intTo(204800));
    }

    private byte[] intTo(int value) throws IOException {
        System.out.println("\n== int: " + value + " ==");
        System.out.println("== int: " + getBinaryString(value) + " ==");
        MessagePack pack = new MessagePack();
        byte[] write = pack.write(value);
        return write;
    }

    @Test
    public void testString() throws Throwable {
        stringTo(getStringByLength("a", 1));
        stringTo(getStringByLength("a", 2));

        stringTo(getStringByLength("a", 1));
        stringTo(getStringByLength("Å", 1));
        stringTo(getStringByLength("瓜", 1));
        stringTo(getStringByLength("😂", 1));
    }

    private String getStringByLength(String item, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(item);
        }
        return sb.toString();
    }

    public void stringTo(String value) throws Throwable {
        MessagePack pack = new MessagePack();

        byte[] result = pack.write(value);

        int max = 10;
        if (value.length() > max) {
            System.out.println("\n== string: " + value.substring(0, max) + "..." + value.length() + " ==");
        } else {
            System.out.println("\n== string: " + value + " ==");
        }
        System.out.println("== string: length = " + value.length() + " ==");
        byte[] utf8Bytes = value.getBytes(StandardCharsets.UTF_8);
        System.out.println("== string: value UTF-8 bytes ==");
        printBytes(utf8Bytes);

        // 开头有 BOM = 0xFEFF
        // byte[] utf16Bytes = value.getBytes(StandardCharsets.UTF_16);
        // System.out.println("== string: value UTF-16 bytes ==");
        // printBytes(utf16Bytes);

        // Java 内部的编码格式
        byte[] utf16beBytes = value.getBytes(StandardCharsets.UTF_16BE);
        System.out.println("== string: value UTF-16BE bytes ==");
        printBytes(utf16beBytes);

        // byte[] utf16leBytes = value.getBytes(StandardCharsets.UTF_16LE);
        // System.out.println("== string: value UTF-16LE bytes ==");
        // printBytes(utf16leBytes);

        System.out.println("== string: msgpack result ==");
        printBytes(result);
    }

    /**
     * 打印字节数组
     *
     * @author D瓜哥 · https://www.diguage.com/
     */
    private void printBytes(byte[] result) {
        if (Objects.isNull(result)) {
            System.out.println(".... bytes is null ....");
            return;
        }

        int min = 0;
        int max = 10;
        System.out.println(".... " + min + " ~ " + max + " ....");
        for (; min < result.length && min < max; min++) {
            printByte(result[min]);
        }
        if (result.length > max) {
            System.out.println("...... " + result.length);
        }
    }

    private void printByte(byte b) {
        String bitx = Integer.toBinaryString(Byte.toUnsignedInt(b));
        String zbits = String.format("%8s", bitx).replace(' ', '0');
        if (0 <= b) {
            System.out.printf("%4d 0x%02X %8s %c %n", b, b, zbits, b);
        } else {
            System.out.printf("%4d 0x%02X %8s %n", b, b, zbits);
        }
    }

    /**
     * 将 long 转化成二进制字符串（前面补0）
     *
     * @author D瓜哥 · https://www.diguage.com/
     */
    private String getBinaryString(long value) {
        String bits = Long.toBinaryString(value);
        char[] chars = String.format("%64s", bits).replace(' ', '0').toCharArray();
        StringBuilder result = new StringBuilder(64 + 7);
        for (int i = 0; i < chars.length; i++) {
            result.append(chars[i]);
            if (i % 8 == 7 && i != chars.length - 1) {
                result.append(",");
            }
        }
        return result.toString();
    }

    /**
     * 将 int 转化成二进制字符串（前面补0）
     *
     * @author D瓜哥 · https://www.diguage.com/
     */
    private String getBinaryString(int value) {
        String bits = Integer.toBinaryString(value);
        char[] chars = String.format("%32s", bits).replace(' ', '0').toCharArray();
        StringBuilder result = new StringBuilder(64 + 7);
        for (int i = 0; i < chars.length; i++) {
            result.append(chars[i]);
            if (i % 8 == 7 && i != chars.length - 1) {
                result.append(",");
            }
        }
        return result.toString();
    }
}
