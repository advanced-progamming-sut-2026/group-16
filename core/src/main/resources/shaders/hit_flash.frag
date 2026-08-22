#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform float u_damageFlash;

void main() {
    vec4 texColor = texture2D(u_texture, v_texCoords);
    if (texColor.a == 0.0) {
        gl_FragColor = texColor * v_color;
        return;
    }
    vec3 lit = mix(texColor.rgb, vec3(1.0), u_damageFlash * 0.7);
    gl_FragColor = vec4(lit, texColor.a) * v_color;
}
