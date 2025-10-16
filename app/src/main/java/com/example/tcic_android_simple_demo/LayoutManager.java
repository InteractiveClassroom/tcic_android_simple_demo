package com.example.tcic_android_simple_demo;

import java.util.ArrayList;
import java.util.List;

/**
 * 布局管理器，用于统一管理横竖屏状态
 */
public class LayoutManager {
    private static boolean isLandscape = false;
    private static List<LayoutChangeListener> listeners = new ArrayList<>();
    
    /**
     * 布局变化监听器接口
     */
    public interface LayoutChangeListener {
        void onLayoutChanged(boolean isLandscape);
    }
    
    /**
     * 设置横竖屏状态
     */
    public static void setLandscape(boolean landscape) {
        if (isLandscape != landscape) {
            isLandscape = landscape;
            // 通知所有监听器
            for (LayoutChangeListener listener : listeners) {
                listener.onLayoutChanged(isLandscape);
            }
        }
    }
    
    /**
     * 获取当前横竖屏状态
     */
    public static boolean isLandscape() {
        return isLandscape;
    }
    
    /**
     * 添加布局变化监听器
     */
    public static void addLayoutChangeListener(LayoutChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * 移除布局变化监听器
     */
    public static void removeLayoutChangeListener(LayoutChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * 清理所有监听器
     */
    public static void clearListeners() {
        listeners.clear();
    }
}