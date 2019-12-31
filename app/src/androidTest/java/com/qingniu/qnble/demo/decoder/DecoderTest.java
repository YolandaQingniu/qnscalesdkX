package com.qingniu.qnble.demo.decoder;

import androidx.test.runner.AndroidJUnit4;

import com.qingniu.qnble.utils.QNLogUtils;
import com.qingniu.scale.utils.ConvertUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author: hekang
 * @description:
 * @date: 2019/1/9 15:18
 */
@RunWith(AndroidJUnit4.class)
public class DecoderTest {

    @Test
    public void testUuid() {
        String d = "fefeba0d2217676ded670200";
        d = ConvertUtils.strToUuid(d);
        QNLogUtils.log("DecoderTest", "testUuid:" + d);
    }
}
