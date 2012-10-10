#version 330

out vec4 out_Color;

void main() {
	float lerpValue = gl_FragCoord.y / 600.0f;
	out_Color = mix(vec4(1.0f, 1.0f, 1.0f, 1.0f),
		vec4(0.2f, 0.2f, 0.2f, 1.0f), lerpValue);
}
