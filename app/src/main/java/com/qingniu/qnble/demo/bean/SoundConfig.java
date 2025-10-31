package com.qingniu.qnble.demo.bean;

/**
 * @author: yxb
 * @description:
 * @date: 2025/10/31
 */
public class SoundConfig {

    public String name;
    public boolean enabled;
    public int volume; // 1~8, 0代表关闭或未设置

    public SoundConfig(String name, boolean enabled, int volume) {
        this.name = name;
        this.enabled = enabled;
        this.volume = volume;
    }

    @Override
    public String toString() {
        return "SoundConfig{" +
                "name='" + name + '\'' +
                ", enabled=" + enabled +
                ", volume=" + volume +
                '}';
    }

}
