package com.diguage.marshal.hession;

import com.caucho.hessian.io.Hessian2Constants;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import com.diguage.Car;
import com.diguage.User;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class HessianTest {

    @Test
    public void test() throws Throwable {
        BigDecimal money = new BigDecimal("1234.56789").setScale(2, BigDecimal.ROUND_HALF_UP);
        int id = 4;
        String name = "diguage";
        Date date = new Date();
        User user = new User(id, name, date, money);
        intTo(id);
        // Hessian 在编码 String 字符串时，在小于32个字符时，前面的长度标志位，
        // 直接使用 int 的二进制表示，取后八位，所以打印出来和数字一样。
        stringTo(name);
        // 直接取时间戳
        dateTo(date);
        bigDecimalTo(money);

        objectTo(user);
    }

    public void bigDecimalTo(BigDecimal value) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output out = getHessian2Output(bos);
        out.writeObject(value);
        out.close();
        byte[] result = bos.toByteArray();
        System.out.println("\n== BigDecimal: " + value + " ==");
        printBytes(result);
    }

    /**
     * 测试 enum 进行 Hessian 序列化
     *
     * @author D瓜哥 · https://www.diguage.com/
     */
    @Test
    public void testEnumOut() throws Throwable {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output out = getHessian2Output(bos);

        // 测试序列化时，去掉这行代码的注释
        // 测试反序列化时，将这行代码注释掉
        out.writeObject(Color.Green);
        out.close();
        byte[] result = bos.toByteArray();

        String base64Hessian = Base64.getEncoder().encodeToString(result);

        System.out.println("\n== Color: " + base64Hessian + " ==");
        printBytes(result);
    }

    /**
     * 测试 enum 新增枚举的 Hessian 反序列化
     *
     * @author D瓜哥 · https://www.diguage.com/
     */
    @Test
    public void testEnumIn() throws Throwable {
        String base64Hessian = "QzAtY29tLmRpZ3VhZ2UubWFyc2hhb" +
                "C5oZXNzaW9uLkhlc3NpYW5UZXN0JENvbG9ykQRuYW1lYAVHcmVlbg==";
        byte[] bytes = Base64.getDecoder().decode(base64Hessian);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        Hessian2Input hessian = getHessian2Input(bis);
        Object object = hessian.readObject();
        System.out.println(object);
    }

    /**
     * @author D瓜哥 · https://www.diguage.com/
     */
    public enum Color {
        Red("red", 0),
        // 测试序列化时，去掉这行代码的注释
        // 测试反序列化时，将这行代码注释掉
        Green("green", 1),
        Blue("blue", 2);

        private String colorName;
        private int colorCode;

        Color(String name, int code) {
            this.colorName = name;
            this.colorCode = code;
        }
    }


    @Test
    public void testBoolean() throws Throwable {
        boolTo(true);
        boolTo(false);
    }

    private void boolTo(boolean bool) throws Throwable {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output out = getHessian2Output(bos);

        System.out.println("\n== Boolean: " + bool + " ==");
        out.writeBoolean(bool);
        out.close();
        byte[] result = bos.toByteArray();
        printBytes(result);
    }

    @Test
    public void testDate() throws Throwable {
        LocalDateTime time = LocalDateTime.of(2022, 5, 1, 23, 27, 48);
        Instant instant = ZonedDateTime.of(time, ZoneId.of("Asia/Shanghai")).toInstant();
        // milli = 1651418868000
        long milli = instant.toEpochMilli();
        Date date = new Date(milli);
        dateTo(date);

        // 代码中，有 time % 60000L == 0 则使用压缩格式
        Date shortDate = new Date(milli - (milli % 60000L));
        dateTo(shortDate);
    }

    public void dateTo(Date date) throws Throwable {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output out = getHessian2Output(bos);

        long time = date.getTime();
        out.writeUTCDate(time); // 从这里也能看出来，Hessian 是直接将日期转换成毫秒数来处理的，简单直接。
        out.close();
        byte[] result = bos.toByteArray();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        System.out.println("\n== Date: " + dateFormat.format(date) + " ==");
        System.out.println("== Date: " + time + "ms ==");
        if (time % 60000L == 0) {
            System.out.printf("== Date: " + getBinaryString(time / 60000) + " m ==%n");
        } else {
            System.out.printf("== Date: " + getBinaryString(time) + " ms ==%n");
        }

        printBytes(result);
    }

    @Test
    public void testDouble() throws Throwable {
        doubleTo(0.0);
        doubleTo(1.0);
        doubleTo(1.1);
        doubleTo(-128.0);
        doubleTo(-129.0);
        doubleTo(127.0);
        doubleTo(128.0);
        doubleTo(-32768.0);
        doubleTo(-32769.0);
        doubleTo(32767.0);
        doubleTo(32768.0);

        // 与 32位浮点数等价的双精度浮点数，可以用四个字节来表示；
        // 从代码来看，假设 newValue = (int) x * 1000，
        // 如果 0.001 * newValue = x，则符合此条件，
        // 将整数 newValue 的二进制位作为 x 的序列化结果
        doubleTo(0.001D);
        doubleTo(-0.001D);
        doubleTo(0.0011D);
        doubleTo(-0.0011D);

        // 这里测试一下协议中提到的 12.25
        doubleTo(12.25);

        doubleTo(Integer.MAX_VALUE / 1000.0);
        doubleTo((1.0D + (long) Integer.MAX_VALUE) / 1000);

        doubleTo(Integer.MIN_VALUE / 1000.0);
        doubleTo(((long) Integer.MIN_VALUE - 1L) / 1000.0);

        // 除了上述的几种情况，其余一律按照 IEEE-754 浮点数标准来处理。
        // 按照双精度来处理
        doubleTo(Float.MIN_VALUE);
        // 按照双精度来处理
        doubleTo(Float.MAX_VALUE);
        // 按照双精度来处理
        doubleTo(Double.MIN_VALUE);
        // 按照双精度来处理
        doubleTo(Double.MAX_VALUE);
    }

    public void doubleTo(double value) throws Throwable {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output out = getHessian2Output(bos);

        out.writeDouble(value);
        out.close();
        byte[] result = bos.toByteArray();

        System.out.println("\n== double: " + value + " ==");
        printBytes(result);
    }


    @Test
    public void testInt() throws Throwable {
        intTo(-16);
        intTo(-17);

        intTo(47);
        intTo(48);

        // 在编码 -16 ~ 47 时，用 10000000（0x80） 表示 -16，
        // 之后就在后六位上逐渐加 1，直到 10111111（0xBF） 来表示 47。
//        for (int i = 0; i <= 47; i++) {
//            intTo(i);
//        }

        // 在编码 -2048 ~ 2047 时，使用两个字节表示。
        // 其中，后面的 12 位用于表示数值。
        // 11000000（0xC0） 00000000（0x00） 表示 -2048，
        // 之后就在后十二位上逐渐加 1，直到
        // 11001111（0xCF） 11111111（0xFF） 表示  2047
        // value = ((code - 0xc8) << 8) + b0;
        // 0xC8 11001000
        // 11000000 - 11001000 = 10000
        // 11001000
        //-00001000

        // 11001111 - 11001000 = 10000
        // 11001000
        //      111

        intTo(-2048);
        intTo(-2049);

        intTo(-2047);
        intTo(-1024);

        intTo(2047);
        intTo(2048);

        // 在编码 -262144 ~ 262143 时，使用三个字节表示。
        // 其中，后面的 19 位用于表示数值。
        // 11010000（0xD0） 00000000（0x00） 00000000（0x00） 表示 -262144，
        // 之后就在后十九位上逐渐加 1，直到
        // 11010111（0xD7） 11111111（0xFF） 11111111（0xFF） 表示  262143
        intTo(-262144);
        intTo(-262145);

        intTo(262143);
        intTo(262144);


        // 演示各个“区间”的分界线
        intTo(Integer.MIN_VALUE);
        // 上下之间的数字，前缀是 0x49，与协议相吻合
        intTo(-262145);
        intTo(-262144);
        // 上下之间的数字，前缀的取值范围是 0xD0 ~ 0xD3
        // 与之相对应的正数的前缀取值范围是 0xD4 ~ 0xD7
        // 与协议中所写的 0xD0 ~ 0xD7 相吻合
        intTo(-2049);
        intTo(-2048);
        // 上下之间的数字，前缀的取值范围是 0xC0 ~ 0xC7
        // 与之相对应的正数的前缀取值范围是 0xC8 ~ 0xCF
        // 与协议中所写的 0xC0 ~ 0xCF 相吻合
        intTo(-17);
        intTo(-16);
        // 上下之间的数字，前缀的取值范围是 0x80~0xBF
        // 与协议中所写的 0x80~0xBF 相吻合
        intTo(47);
        intTo(48);
        // 上下之间的数字，前缀的取值范围是 0xC8 ~ 0xCF
        // 与之相对应的负数的前缀取值范围是 0xC0 ~ 0xC7
        // 与协议中所写的 0xC0 ~ 0xCF 相吻合
        intTo(2047);
        intTo(2048);
        // 上下之间的数字，前缀的取值范围是 0xD4 ~ 0xD7
        // 与之相对应的负数的前缀取值范围是 0xD0 ~ 0xD3
        // 与协议中所写的 0xD0 ~ 0xD7 相吻合
        intTo(262143);
        intTo(262144);
        // 上下之间的数字，前缀是 0x49，与协议相吻合
        intTo(Integer.MAX_VALUE);


    }

    public void intTo(int value) throws Throwable {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output out = getHessian2Output(bos);

        out.writeInt(value);
        out.close();
        byte[] result = bos.toByteArray();

        System.out.println("\n== int: " + value + " ==");
        System.out.println("== int: " + getBinaryString(value) + " ==");
        printBytes(result);
    }

    @Test
    public void testLong() throws Throwable {
        // 演示各个“区间”的分界线
        longTo(Long.MIN_VALUE);
        // 上下之间的数字，前缀是 0x4C，与协议相吻合
        longTo(((long) Integer.MIN_VALUE) - 1L);
        longTo((long) Integer.MIN_VALUE);
        // 上下之间的数字，前缀是 0x59，不是协议中所写的 0x4C。
        longTo(-262145L);
        longTo(-262144L);
        // 上下之间的数字，前缀的取值范围是 0x38 ~ 0x3B
        // 与之相对应的正数的前缀取值范围是 0x3C ~ 0x3F
        // 与协议中所写的 0x38 ~ 0x3F 相吻合
        longTo(-2049L);
        longTo(-2048L);
        // 上下之间的数字，前缀的取值范围是 0xF0 ~ 0xF7
        // 与之相对应的正数的前缀取值范围是 0xF8 ~ 0xFF
        // 与协议中所写的 0xF0 ~ 0xFF 相吻合
        longTo(-9L);
        longTo(-8L);
        // 上下之间的数字，前缀的取值范围是 0xD8 ~ 0xEF
        // 与协议中所写的 0xD8 ~ 0xEF 相吻合
        longTo(15L);
        longTo(16L);
        // 上下之间的数字，前缀的取值范围是 0xF8 ~ 0xFF
        // 与之相对应的负数的前缀取值范围是 0xF0 ~ 0xF7
        // 与协议中所写的 0xF0 ~ 0xFF 相吻合
        longTo(2047L);
        longTo(2048L);
        // 上下之间的数字，前缀的取值范围是 0x3C ~ 0x3F
        // 与之相对应的负数的前缀取值范围是 0x38 ~ 0x3B
        // 与协议中所写的 0x38 ~ 0x3F 相吻合
        longTo(262143L);
        longTo(262144L);
        // 上下之间的数字，前缀是 0x59，不是协议中所写的 0x4C。
        longTo((long) Integer.MAX_VALUE);
        longTo(((long) Integer.MAX_VALUE) + 1L);
        // 上下之间的数字，前缀是 0x4C，与协议相吻合
        longTo(Long.MAX_VALUE);
    }

    public void longTo(long value) throws Throwable {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output out = getHessian2Output(bos);

        out.writeLong(value);
        out.close();
        byte[] result = bos.toByteArray();

        System.out.println("\n== long: " + value + " ==");
        System.out.println("== long: " + getBinaryString(value) + " ==");
        printBytes(result);
    }

    @Test
    public void testString() throws Throwable {
//        // 单字节字符串
//        stringTo("D");
//        // 双字节字符串
//        stringTo("Å");
//        // 三字节字符串
//        stringTo("瓜");
//        // 四字节字符串
//        stringTo("😂");
//        // 😂 = U+1f602
//        // 第一步，先将 Unicode 转换成 UTF-16 编码；
//        //      对于超过 BMP 的字符，UTF-16 会将其拆
//        //      分成两个字符来处理。由于 Java 内部，char
//        //      类型的数据就是使用 UTF-16 编码的，所以，
//        //      这一步已经提前完成，无需再做处理。
//        // (打开调试，查看 char 的内容即可确认)
//        // 这里演示一下从 Unicode 转 UTF-16 的过程：
//        // U+1f602 - 0x10000 = 0x0f602
//        // 0x0f602 = 00 0011 1101, 10 0000 0010
//        //   00 0011 1101 + 0XD800
//        // =         00 0011 1101
//        //   + 11011000 0000 0000
//        // ----------------------
//        // =   11011000 0011 1101
//        // = d83d
//        //
//        //   10 0000 0010 + 0xDC00
//        // =         10 0000 0010
//        //   + 11011100 0000 0000
//        // ----------------------
//        // =   11011110 0000 0010
//        // = de02
//        //
//        // 第二步，`char` 值大于等于 `0x800` 的 `char`，会将其
//        //      “值”当做 Unicode 然后转换成“3个字节的UTF-8”。
//        //      如果是需要两个 `char` 表示的字符，则当做两个 “Unicode 值”
//        //      处理，则 会转成两个“3 个字节的 UTF-8”，就是六个字节。
//        // 注：这里的“3个字节的UTF-8”，并不是通常说的 UTF-8 编码，
//        //     只是借用了“3个字节的UTF-8”的编码格式，徒有其表而已。
//        // 11011000 0011 1101 → 11101101 10100000 10111101
//        // 11011110 0000 0010 → 11101101 10111000 10000010
//        // 转换算法见上面的“Unicode 与 UTF-8 的转换”图表。
//
//        // 大家可以试试 👍 的转换： 👍 = U+1F44D
//        stringTo("👍");
//
//        // 更长久的长字符串处理示例
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        Hessian2Output out = getHessian2Output(bos);
//        out.writeString("D瓜哥");
//        out.writeString("https://www.diguage.com");
//        out.writeString("👍👍👍，老李卖瓜，自卖自夸，😂😂😂");
//        out.close();
//        byte[] hessianBytes = bos.toByteArray();
//        ByteArrayInputStream bais = new ByteArrayInputStream(hessianBytes);
//        Hessian2Input hessian2Input = getHessian2Input(bais);
//        String s1 = hessian2Input.readString();
//        System.out.println(s1);
//        String s2 = hessian2Input.readString();
//        System.out.println(s2);
//        String s3 = hessian2Input.readString();
//        System.out.println(s3);
//        hessian2Input.close();
//        bais.close();
//
        // 同时序列化由不同个字节组成一个字符，
        // 可以看出，结果中的长度和字符数量无关，
        // 而是和  String.length() 相关。
        // 而  String.length() 的数量等于内部 char 数量，
        // 一个字符由多少个 char 表示，长度就是多少。
        // 换句话说：
        // BMP 以内的字符，每个字符长度为 1；
        // BMP 之外的字符，每个字符长度为 2.
//        stringTo("a");
//        stringTo("Å");
//        stringTo("瓜");
//        stringTo("😂");


        // 0x00~0x31 0~31
        // 32~255 的前置标志位是 0x30，然后从 256 开始，每隔 256 个一个标志位。
        // 0x30 32~255
        // 0x31 256~511
        // 0x32 512~767
        // 0x33 768~1023
        // 之所以这样，是因为使用一个字节来表示“长度”；而 0、1、2、3 保存在前置标志位的末尾。
        // 这里又有一个错误：Hessian2Constants.STRING_SHORT_MAX = 0x3ff 最大值是 1023，
        // 对应 0x33。所以，0x34 不会出现的。超过 1023 之后，前置标志位就是 S 了。
        stringTo("");
        // 0~31 之间，直接使用一个字符进行编码
        stringTo(getStringByLength("a", 1));
        stringTo(getStringByLength("Å", 1));
        stringTo(getStringByLength("瓜", 1));
        stringTo(getStringByLength("😂", 1));
        stringTo(getStringByLength("a", 31));
        stringTo(getStringByLength("a", 32));
        // 32~255 之间，使用一个前缀标志符 0x30 + 一个字符进行编码
        stringTo(getStringByLength("a", 255));
        stringTo(getStringByLength("a", 256));
        // 256~511 之间，使用一个前缀标志符 0x31 + 一个字符进行编码
        stringTo(getStringByLength("a", 511));
        stringTo(getStringByLength("a", 512));
        // 512~767 之间，使用一个前缀标志符 0x32 + 一个字符进行编码
        stringTo(getStringByLength("a", 767));
        stringTo(getStringByLength("a", 768));
        // 768~1023 之间，使用一个前缀标志符 0x33 + 一个字符进行编码
        stringTo(getStringByLength("a", 1023));
        stringTo(getStringByLength("a", 1024));
        // 1024~32768 之间，使用一个前缀标志符 S + 一个字符进行编码
//        System.out.printf("0x%02X\n", BC_STRING_SHORT + (255 >> 8));
//        System.out.printf("0x%02X\n", BC_STRING_SHORT + (256 >> 8));
//        System.out.printf("0x%02X\n", BC_STRING_SHORT + (511 >> 8));
//        System.out.printf("0x%02X\n", BC_STRING_SHORT + (512 >> 8));
//        System.out.printf("0x%02X\n", BC_STRING_SHORT + ((512 + 256 - 1) >> 8));
//        System.out.printf("0x%02X\n", BC_STRING_SHORT + ((512 + 256) >> 8));
//        System.out.printf("0x%02X\n", BC_STRING_SHORT + (1023 >> 8));
//        System.out.printf("0x%02X\n", BC_STRING_SHORT + (1024 >> 8));

        // 测试字符串分块
        // 根据协议中对于字符串的“长度为 0-65535 的字符串”的描述，65535 为分块大小的界限。
        // 那么，长度为 65535 应该不分块，a*65535 序列化后，长度应该是 65535 + 3。
        // 但是，实际实验的结果为 65535 + 6。那么协议描述有问题。
        // stringTo(getStringByLength("a", 65535));
        //
        // 查看代码，分块相关代码的判断条件是 length > 0x8000，那么分块边界
        // 为 0x8000 = 32768。根据输出，跟代码是吻合的。
        // 另外，协议中“`x53`（`S`）表示最终块” 的表述不正确！最终块的前置标志符是什么，
        // 得看截取完前面的分块之后，剩余的字符的个数。如果大于 1023 才会以 `x53`（`S`）开头。
        // 最终块的前置标志符。
        stringTo(getStringByLength("a", 32768));
        stringTo(getStringByLength("a", 32768 + 1));
        stringTo(getStringByLength("a", 32768 + 32));
        stringTo(getStringByLength("a", 32768 + 256));
        stringTo(getStringByLength("a", 32768 + 512));
        stringTo(getStringByLength("a", 32768 + 768));
        stringTo(getStringByLength("a", 32768 + 1024));
    }

    private String getStringByLength(String item, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(item);
        }
        return sb.toString();
    }

    public void stringTo(String value) throws Throwable {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output out = getHessian2Output(bos);

        out.writeString(value);
        out.close();
        byte[] result = bos.toByteArray();

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

        System.out.println("== string: hessian result ==");
        printBytes(result);
    }

    /**
     * 测试 null 进行 Hessian 序列化
     *
     * @author D瓜哥 · https://www.diguage.com/
     */
    @Test
    public void testNull() throws Throwable {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output out = getHessian2Output(bos);
        // 字符串： null
        out.writeString(null);
        // 字节数组： null
        out.writeBytes(null);
        // 对象： null
        out.writeObject(null);
        out.close();
        byte[] result = bos.toByteArray();

        System.out.println("\n== null ==");
        printBytes(result);
    }


    /**
     * 测试字节数组进行 Hessian 序列化
     *
     * @author D瓜哥 · https://www.diguage.com/
     */
    @Test
    public void testByteArray() throws Throwable {
        bytesTo(new byte[]{});
        // 0~15 之间，直接使用一个字符进行编码， 0x20~0x2F
        bytesTo(getBytesByLength((byte) '@', 15));
        bytesTo(getBytesByLength((byte) '@', 16));
        // 16~255 之间，使用一个前缀标志符 0x34 + 两个字符进行编码
        bytesTo(getBytesByLength((byte) '@', 255));
        bytesTo(getBytesByLength((byte) '@', 256));
        // 256~511 之间，使用一个前缀标志符 0x35 + 两个字符进行编码
        bytesTo(getBytesByLength((byte) '@', 511));
        bytesTo(getBytesByLength((byte) '@', 512));
        // 512~767 之间，使用一个前缀标志符 0x36 + 两个字符进行编码
        bytesTo(getBytesByLength((byte) '@', 767));
        bytesTo(getBytesByLength((byte) '@', 768));
        // 768~1023 之间，使用一个前缀标志符 0x37 + 两个字符进行编码
        bytesTo(getBytesByLength((byte) '@', 1023));
        bytesTo(getBytesByLength((byte) '@', 1024));
        // 1024~8189 之间，使用一个前缀标志符 0x42(B) + 两个字符进行编码
        bytesTo(getBytesByLength((byte) '@', 8 * 1024 - 3));
        bytesTo(getBytesByLength((byte) '@', 8 * 1024 - 3 + 1));
        // 8190~8204 之间，
        // 先使用一个前缀标志符 0x41(A) + 两个字符进行编码前 8 * 1024 - 3 = 8189 个字节
        // 再使用一个字符进行编码， 0x21~0x2F。
        // 后续长度的字节数组，都是按照如此编码：
        // 首先使用 0x41(A) + 两个字符进行编码前 N * 8189 个字节
        // 然后，剩余编码按照 0 ~ 8189 个字节的编码规则进行编码。
        bytesTo(getBytesByLength((byte) '@', 8 * 1024 - 3 + 15));
        bytesTo(getBytesByLength((byte) '@', 8 * 1024 - 3 + 16));
        // 8205~8445 之间，使用一个前缀标志符 0x34 + 一个字符进行编码
        bytesTo(getBytesByLength((byte) '@', 8 * 1024 - 3 + 256));
        bytesTo(getBytesByLength((byte) '@', 8 * 1024 - 3 + 512));
        bytesTo(getBytesByLength((byte) '@', 8 * 1024 - 3 + 768));
        bytesTo(getBytesByLength((byte) '@', 8 * 1024 - 3 + 1024));
        bytesTo(getBytesByLength((byte) '@', (8 * 1024 - 3) * 2));
        bytesTo(getBytesByLength((byte) '@', (8 * 1024 - 3) * 2 + 1));
    }

    /**
     * 生成指定长度的字节数组
     *
     * @author D瓜哥 · https://www.diguage.com/
     */
    private byte[] getBytesByLength(byte b, int len) {
        byte[] result = new byte[len];
        Arrays.fill(result, b);
        return result;
    }

    /**
     * 字节数组序列化
     *
     * @author D瓜哥 · https://www.diguage.com/
     */
    public void bytesTo(byte[] bytes) throws Throwable {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output out = getHessian2Output(bos);

        out.writeBytes(bytes);
        out.close();
        byte[] result = bos.toByteArray();

        System.out.println("\n== byte array: length=" + bytes.length + " ==");
        printBytes(bytes);
        System.out.println("== byte array: hessian result ==");
        printBytes(result);
    }


    @Test
    public void testObject1() throws Throwable {
        Car value = new Car("diguage", 47);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output out = getHessian2Output(bos);

        // 在序列化实例对象时，
        // 首先，序列化实例对象对应的类定义：
        // ①类型（字符串形式）②字段数量③各个属性名称
        // 其次，序列化实例对象
        // ①根据类型找到对应的类型编号②依次序列化实例属性
        // 关于编号编码：
        // 1、在 ref ∈ [0, 15] 时，编码为：BC_OBJECT_DIRECT（0x60）+ ref
        // 2、在 ref ∈ [16, ] 时，编码为 ①O ②ref（以int编码）
        // 类型编号没有前置存储，是根据在类型在序列化出现的顺序来排序的，从 0 开始，依次递增。
        out.writeObject(value);
        // 序列化两次，查看差异
        // 根据实验发现：重复对象会使用前置标志位 0x51（Q）+ 编号来处理，减少数据量。
        // 引用编号没有前置存储，是根据在引用在序列化出现的顺序来排序的，从 0 开始，依次递增。
        out.writeObject(value);
        out.close();
        byte[] result = bos.toByteArray();

        System.out.println("\n== Object: " + value.getClass().getName() + "  ==");
        System.out.println(toJson(value));
        System.out.println("== byte array: hessian result ==");
        printBytes(result);
    }

    @Test
    public void testByte() {
        printByte((byte) Hessian2Constants.BC_OBJECT);
    }

    @Test
    public void testObject2() throws Throwable {
        Object value = new Object();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output out = getHessian2Output(bos);

        // 在序列化实例对象时，
        // 首先，序列化实例对象对应的类定义：
        // ①类型（字符串形式）②字段数量③各个属性名称
        // 其次，序列化实例对象
        // ①根据类型找到对应的类型编号②依次序列化实例属性
        // 关于编号编码：
        // 1、在 ref ∈ [0, 15] 时，编码为：BC_OBJECT_DIRECT（0x60）+ ref
        // 2、在 ref ∈ [16, ] 时，编码为 ①O ②ref（以int编码）
        // 类型编号没有前置存储，是根据在类型在序列化出现的顺序来排序的，从 0 开始，依次递增。
        out.writeObject(value);
        // 序列化两次，查看差异
        // 根据实验发现：重复对象会使用前置标志位 0x51（Q）+ 编号来处理，减少数据量。
        // 引用编号没有前置存储，是根据在引用在序列化出现的顺序来排序的，从 0 开始，依次递增。
        out.writeObject(value);
        out.close();
        byte[] result = bos.toByteArray();

        System.out.println("\n== Object: " + value.getClass().getName() + "  ==");
        System.out.println(toJson(value));
        System.out.println("== byte array: hessian result ==");
        printBytes(result);
    }

    /**
     * 对象序列化
     *
     * @author D瓜哥 · https://www.diguage.com/
     */
    public void objectTo(Object value) throws Throwable {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output out = getHessian2Output(bos);

        out.writeObject(value);
        out.close();
        byte[] result = bos.toByteArray();

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
        String json = toJson(value);
        System.out.println("== object: json length=" + json.length() + " ==");
        System.out.println(json);
        System.out.println("== object: msgpack result ==");
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
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    public void testIntArray() throws Throwable {
        // 在处理长度为 [0, 7] 的数组时：
        // ①前置标志位： BC_LIST_DIRECT（0x70）+ length
        //   范围：0x70(p) ~ 0x77(w)
        // ②类型（字符串形式）
        // ③逐个数组元素
        // 注意：如果数组为空，则没有第③项
        objectTo(new int[]{});
        objectTo(new int[]{0});
        objectTo(new int[]{0, 1, 2, 3, 4, 5, 6});
        // 在处理长度为 [8, 0] 的数组时：
        // ①使用前置标志位 V 表示
        // ②类型（字符串形式）
        // ③长度length
        // ④逐个数组元素
        objectTo(new int[]{0, 1, 2, 3, 4, 5, 6, 7});
    }

    /**
     * 测试对象数组的序列化
     *
     * @author D瓜哥 · https://www.diguage.com/
     */
    @Test
    public void testObjectArray() throws Throwable {
        // 在处理长度为 [0, 7] 的数组时：
        // ①前置标志位： BC_LIST_DIRECT（0x70）+ length
        //   范围：0x70(p) ~ 0x77(w)
        // ②类型（字符串形式）
        // ③逐个数组元素
        // 注意：如果数组为空，则没有第③项
        Car c = new Car("diguage", 47);
        objectTo(new Car[]{});
        objectTo(new Car[]{c});
        objectTo(new Car[]{c, c, c, c, c, c, c});
        // 在处理长度为 [8, 0] 的数组时：
        // ①使用前置标志位 V 表示
        // ②类型（字符串形式）
        // ③长度length
        // ④逐个数组元素
        // 由于我这里使用了相同的元素，所以，
        // 除第一个元素外，其他元素都试用引用编号来编码。
        objectTo(new Car[]{c, c, c, c, c, c, c, c});
    }

    /**
     * 测试 ArrayList 的序列化
     *
     * @author D瓜哥 · https://www.diguage.com/
     */
    @Test
    public void testIntArrayList() throws Throwable {
        // 在处理长度为 [0, 7] 的 ArrayList 时：
        // ①前置标志位： BC_LIST_DIRECT_UNTYPED（0x78）+ length
        //   范围：0x78(x) ~ 0x7F
        // ②逐个集合元素
        // 注意：如果集合为空，则没有第②项
        List<Integer> al0 = new ArrayList<>();
        objectTo(al0);

        List<Integer> ints1 = Arrays.asList(0);
        List<Integer> al1 = new ArrayList<>(ints1);
        objectTo(al1);

        List<Integer> ints7 = Arrays.asList(0, 1, 2, 3, 4, 5, 6);
        List<Integer> al7 = new ArrayList<>(ints7);
        objectTo(al7);

        // 在处理长度为 [8, 0] 的 ArrayList 时：
        // ①使用前置标志位 0x58（X） 表示
        // ②长度length
        // ③逐个集合元素
        List<Integer> ints8 = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
        List<Integer> al8 = new ArrayList<>(ints8);
        objectTo(al8);
    }

    /**
     * 测试 LinkedList 的序列化
     *
     * @author D瓜哥 · https://www.diguage.com/
     */
    @Test
    public void testIntLinkedList() throws Throwable {
        // 在处理长度为 [0, 7] 的 LinkedList 时，
        // ①前置标志位： BC_LIST_DIRECT（0x70）+ length
        //   范围：0x70(p) ~ 0x77(w)
        // ②类型（字符串形式）
        // ③逐个数组元素
        // 注意：如果数组为空，则没有第③项
        List<Integer> ll0 = new LinkedList<>();
        objectTo(ll0);

        List<Integer> ints1 = Arrays.asList(0);
        List<Integer> ll1 = new LinkedList<>(ints1);
        objectTo(ll1);

        List<Integer> ints7 = Arrays.asList(0, 1, 2, 3, 4, 5, 6);
        List<Integer> ll7 = new LinkedList<>(ints7);
        objectTo(ll7);

        // 在处理长度为 [8, 0] 的 LinkedList 时，
        // ①使用前置标志位 V 表示
        // ②类型（字符串形式）
        // ③数组长度length
        // ④逐个数组元素
        List<Integer> ints8 = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
        List<Integer> ll8 = new LinkedList<>(ints8);
        objectTo(ll8);
    }

    /**
     * 测试 LinkedList 的序列化
     *
     * @author D瓜哥 · https://www.diguage.com/
     */
    @Test
    public void testIntHashSet() throws Throwable {
        // 在处理长度为 [0, 7] 的 LinkedList 时，
        // ①前置标志位： BC_LIST_DIRECT（0x70）+ length
        //   范围：0x70(p) ~ 0x77(w)
        // ②类型（字符串形式）
        // ③逐个数组元素
        // 注意：如果数组为空，则没有第③项
        Set<Integer> hs0 = new HashSet<>();
        objectTo(hs0);

        List<Integer> ints1 = Arrays.asList(0);
        Set<Integer> hs1 = new HashSet<>(ints1);
        objectTo(hs1);

        List<Integer> ints7 = Arrays.asList(0, 1, 2, 3, 4, 5, 6);
        Set<Integer> hs7 = new HashSet<>(ints7);
        objectTo(hs7);

        // 在处理长度为 [8, 0] 的 LinkedList 时，
        // ①使用前置标志位 V 表示
        // ②类型（字符串形式）
        // ③数组长度length
        // ④逐个数组元素
        List<Integer> ints8 = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
        Set<Integer> hs8 = new HashSet<>(ints8);
        objectTo(hs8);
    }

    /**
     * 测试 Iterator 的序列化
     *
     * @author D瓜哥 · https://www.diguage.com/
     */
    @Test
    public void testIntIterator() throws Throwable {
        // 处理 Iterator 和 Enumeration 时，
        // ①前置标志位 BC_LIST_VARIABLE_UNTYPED（0x57）
        // ②遍历 Iterator，逐个写入元素。为空则不写入。
        // ③写入结束标志位 BC_END（Z）
        List<Integer> al0 = new ArrayList<>();
        objectTo(al0.iterator());

        List<Integer> ints1 = new ArrayList<>(Arrays.asList(0));
        objectTo(ints1.iterator());

        List<Integer> ints2 = Arrays.asList(0, 1);
        objectTo(ints2.iterator());
    }

    @Test
    public void testList() throws Throwable {
        /*
list       ::= x55 type value* 'Z'   # 可变长度链表，类似 List
           ::= 'V' type int value*   # 固定长度链表，类似 数组  -- 长度超过 7 的数组
           ::= x57 value* 'Z'        # 可变长度的无类型链表 -- iterator
           ::= x58 int value*        # 固定长度的无类型链表 -- 长度超过9？的ArrayList
           ::= [x70-77] type value*  # 固定长度的有类型链表 -- 数组，Arrays.asList(1, 2)，HashSet，LinkedList
           ::= [x78-7f] value*       # 固定长度的无类型链表 -- ArrayList
         */
        // 1. ArrayList -- CollectionSerializer
        // 2. LinkedList -- CollectionSerializer
        // 3. Set  -- CollectionSerializer
        // 4. array  -- ArraySerializer
        // 5. Iterator -- IteratorSerializer
        // 6. Enumeration -- EnumerationSerializer


//        Car c1 = new Car("diguage", 47);
//        objectTo(new Car[]{c1});
//        objectTo(new Car[]{c1, c1, c1, c1, c1, c1, c1});
//        objectTo(new Car[]{c1, c1, c1, c1, c1, c1, c1, c1});

        List<Integer> al1 = new ArrayList<>();
        al1.add(1);
        objectTo(al1);

        List<Integer> al9 = new ArrayList<>();
        al9.add(1);
        al9.add(2);
        al9.add(3);
        al9.add(4);
        al9.add(5);
        al9.add(6);
        al9.add(7);
        al9.add(8);
        al9.add(9);
        objectTo(al9);

        objectTo(Arrays.asList(1, 2));

        objectTo(al1.iterator());

        Set<Integer> hashSet = new HashSet<>();
        hashSet.add(1);
        hashSet.add(2);
        objectTo(hashSet);

        List<Integer> ll1 = new LinkedList<>();
        ll1.add(1);
        objectTo(ll1);

//        List<Car> carList = new ArrayList<>();
//        carList.add(c1);
//        objectTo(carList);
    }

    /**
     * 测试 HashMap 的序列化
     *
     * @author D瓜哥 · https://www.diguage.com/
     */
    @Test
    public void testHashMap() throws Throwable {
        // 由 MapSerializer 来处理。分三步来处理：
        // 1、首先，写入前置标志位 BC_MAP_UNTYPED = 'H'
        // 2、其次，遍历 Map.Entry，并将其序列化：①Key ②Value
        // 3、最后，写入结束标志位 BC_END = 'Z'
        Map<Integer, Car> map = new HashMap<>();
        map.put(1, new Car("diguage", 47));
        objectTo(map);
    }

    /**
     * 测试 TreeMap 的序列化
     *
     * @author D瓜哥 · https://www.diguage.com/
     */
    @Test
    public void testTreeMap() throws Throwable {
        // 由 MapSerializer 来处理。分三步来处理：
        // 1、首先，①写入前置标志位 BC_MAP = 'M' ②写入 Map 的类型（字符串）
        // 2、其次，遍历 Map.Entry，并将其序列化：①Key ②Value
        // 3、最后，写入结束标志位 BC_END = 'Z'
        Car c = new Car("diguage", 47);
        Map<Integer, Car> map = new TreeMap<>();
        map.put(1, c);
        objectTo(map);
    }

    /**
     * 创建 Hessian2Input 对象，以便用于反序列化
     *
     * @author D瓜哥 · https://www.diguage.com/
     */
    private Hessian2Input getHessian2Input(InputStream is) {
        SerializerFactory serializerFactory = new SerializerFactory();
        serializerFactory.setAllowNonSerializable(true);
        Hessian2Input result = new Hessian2Input(is);
        result.setSerializerFactory(serializerFactory);
        return result;
    }

    /**
     * 创建 Hessian2Output 对象，以便用于序列化
     *
     * @author D瓜哥 · https://www.diguage.com/
     */
    private Hessian2Output getHessian2Output(OutputStream stream) {
        SerializerFactory serializerFactory = new SerializerFactory();
        serializerFactory.setAllowNonSerializable(true);
        Hessian2Output result = new Hessian2Output(stream);
        result.setSerializerFactory(serializerFactory);
        return result;
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
        int chunk = 0x8000;
        int byteChunk = 8 * 1024;
        if (0 < result.length && chunk < result.length & result[0] == 'R') {
            for (int i = 0; i < result.length; i += (chunk + 3)) {
                int min = Math.max(i - 1, 0);
                int max = Math.min(i + 4, result.length);
                System.out.println(".... " + min + " ~ " + max + " ....");
                for (; min < max; min++) {
                    printByte(result[min]);
                }
            }
            System.out.println("...... " + result.length);
        } else if (0 < result.length && byteChunk < result.length && result[0] == 'A') {
            for (int i = 0; i < result.length; i += byteChunk) {
                int min = Math.max(i - 1, 0);
                int max = Math.min(i + 4, result.length);
                System.out.println(".... " + min + " ~ " + max + " ....");
                for (; min < max; min++) {
                    printByte(result[min]);
                }
            }
            System.out.println("...... " + result.length);
        } else if (result.length > 0 && (result[0] == 'C' // class def
                // List
                || result[0] == 0x55 || result[0] == 'V'
                || result[0] == 0x57 || result[0] == 0x58
                || (0x70 <= result[0] && result[0] <= ((byte) 0x7F))
                // Map
                || result[0] == 'M' || result[0] == 'H'
                // object
                || result[0] == 'O'
                || (0x60 <= result[0] && result[0] <= ((byte) 0x6F)))) {
            int min = 0;
            int max = result.length;
            System.out.println(".... " + min + " ~ " + max + " ....");
            for (; min < result.length; min++) {
                printByte(result[min]);
            }
        } else {
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
