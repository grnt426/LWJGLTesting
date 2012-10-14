#version 330

out vec4 outputColor;

uniform float height;
uniform float width;

void main(){
    outputColor = vec4(gl_FragCoord.x / width, gl_FragCoord.y / height,
    	0.0f, 1.0f);
}