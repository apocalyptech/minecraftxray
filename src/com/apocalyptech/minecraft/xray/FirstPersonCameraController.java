/**
 * Copyright (c) 2010-2011, Vincent Vollers and Christopher J. Kucera
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Minecraft X-Ray team nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL VINCENT VOLLERS OR CJ KUCERA BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.apocalyptech.minecraft.xray;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;


/***
 * Utility class which mimicks a first person shooters camera perspective
 * @author Vincent
 */
public class FirstPersonCameraController {
    private Vector3f    position    = null;
    private float       yaw         = 0.0f;
    private float       pitch       = 0.0f;
   
    /***
     * Create a camera at location x,y,z facing down the z axis
     * @param x
     * @param y
     * @param z
     */
    public FirstPersonCameraController(float x, float y, float z)
    {
        //instantiate position Vector3f to the x y z params.
        position = new Vector3f(x, y, z);
    }
    
    //increment the camera's current yaw rotation
    public void incYaw(float amount)
    {
        yaw += amount;
    }

    public float getYaw() {
    	return this.yaw;
    }
    
    //increment the camera's current yaw rotation
    public void incPitch(float amount)
    {
        pitch += amount;
    }
    
    public float getPitch() {
    	return this.pitch;
    }
    
   
    public void walkForward(float distance, boolean camera_lock)
    {
        position.x -= distance * (float)Math.sin(Math.toRadians(yaw));
        position.z += distance * (float)Math.cos(Math.toRadians(yaw));
        if (!camera_lock)
        {
        	position.y += distance * (float)Math.sin(Math.toRadians(pitch));
        }
    }

 
    public void walkBackwards(float distance, boolean camera_lock)
    {
        position.x += distance * (float)Math.sin(Math.toRadians(yaw));
        position.z -= distance * (float)Math.cos(Math.toRadians(yaw));
        if (!camera_lock)
        {
        	position.y -= distance * (float)Math.sin(Math.toRadians(pitch));
        }
    }

    public void strafeLeft(float distance)
    {
        position.x -= distance * (float)Math.sin(Math.toRadians(yaw-90));
        position.z += distance * (float)Math.cos(Math.toRadians(yaw-90));
    }

    public void strafeRight(float distance)
    {
        position.x -= distance * (float)Math.sin(Math.toRadians(yaw+90));
        position.z += distance * (float)Math.cos(Math.toRadians(yaw+90));
    }
    
    public void moveUp(float distance) {
    	position.y -= distance;
    }
 
    public void applyCameraTransformation()
    {
        //roatate the pitch around the X axis
        GL11.glRotatef(pitch, 1.0f, 0.0f, 0.0f);
        //roatate the yaw around the Y axis
        GL11.glRotatef(yaw, 0.0f, 1.0f, 0.0f);
        //translate to the position vector's location
        GL11.glTranslatef(position.x, position.y, position.z);
    }
    
    public Vector3f getPosition() {
    	return this.position;
    }

	public void setYawAndPitch(float yaw, float pitch) {
		this.yaw = yaw;
		this.pitch = pitch;
	}
	
	/**
	 * Processes the given Nether warp factor (should generally be 8.0f or
	 * 1/8.0f, depending on the direction we're headed).
	 * 
	 * @param multiplier
	 */
	public void processNetherWarp(float multiplier)
	{
		this.position.x *= multiplier;
		this.position.z *= multiplier;
	}
}
