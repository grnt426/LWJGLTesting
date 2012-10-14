#version 330

layout (location = 0) in vec4 position;

const vec4 initPosition = vec4(0.0f, 0.0f, 0.0f, 0.0f);
const vec4 initVelocity = vec4(0.163f, 0.6f, 0.0f, 0.0f);
const vec4 initAcceleration = vec4(0.0f, -0.098f, 0.0f, 0.0f);

uniform float time;

void main(){
	vec4 offset = initPosition + initVelocity * time
		+ 0.5f * initAcceleration * time * time;
    gl_Position = position + offset;
}