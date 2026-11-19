package com.shazwan.idash3;

import android.graphics.RuntimeShader;
import android.os.Build;

/** AGSL caustic/specular animation. API 33+ ONLY; caller must check SDK first. */
@android.annotation.TargetApi(33)
final class UltraGlassAgsl {
    private UltraGlassAgsl() {}

    private static final String PROGRAM =
        "uniform float2 resolution;\n" +
        "uniform float time;\n" +
        "half4 main(float2 pos) {\n" +
        "  float2 uv = pos / max(resolution, float2(1.0, 1.0));\n" +
        "  float y = 0.13 + 0.025 * sin(uv.x * 13.0 + time * 0.63);\n" +
        "  float caustic = exp(-pow((uv.y - y) * 12.0, 2.0));\n" +
        "  float sweep = exp(-pow((uv.x - (0.5 + 0.38 * sin(time * 0.34))) * 5.0, 2.0));\n" +
        "  float intensity = clamp(0.22 * caustic + 0.08 * sweep, 0.0, 0.28);\n" +
        "  float3 blue = float3(0.14, 0.78, 1.00);\n" +
        "  float3 violet = float3(0.98, 0.39, 1.00);\n" +
        "  float3 colour = mix(blue, violet, smoothstep(0.15, 0.90, uv.x));\n" +
        "  return half4(half3(colour * intensity), half(intensity));\n" +
        "}\n";

    static RuntimeShader create() {
        if (Build.VERSION.SDK_INT < 33) throw new IllegalStateException("AGSL requires Android 13+");
        return new RuntimeShader(PROGRAM);
    }

    static void update(RuntimeShader shader,float w,float h,float seconds) {
        shader.setFloatUniform("resolution",w,h);
        shader.setFloatUniform("time",seconds);
    }
}
