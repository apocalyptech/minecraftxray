/**
 * Copyright (c) 2010-2012, Christopher J. Kucera
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

import com.apocalyptech.minecraft.xray.dialog.ExceptionDialog;

public class BlockTypeLoadException extends Exception {
	private Exception origException;
	private String extraStatus1;
	private String extraStatus2;
	public BlockTypeLoadException()
	{
		this.origException = null;
		this.extraStatus1 = ExceptionDialog.getExtraStatus1();
		this.extraStatus2 = ExceptionDialog.getExtraStatus2();
	}
	public BlockTypeLoadException(String message)
	{
		super(message);
		this.origException = null;
		this.extraStatus1 = ExceptionDialog.getExtraStatus1();
		this.extraStatus2 = ExceptionDialog.getExtraStatus2();
	}
	public BlockTypeLoadException(String message, Exception origException)
	{
		super(message);
		this.origException = origException;
		this.extraStatus1 = ExceptionDialog.getExtraStatus1();
		this.extraStatus2 = ExceptionDialog.getExtraStatus2();
	}
	public Exception getOrigException()
	{
		return this.origException;
	}
	public String toString()
	{
		StringBuffer errStr = new StringBuffer();
		if (this.extraStatus1 != null)
		{
			errStr.append(this.extraStatus1 + " - ");
		}
		if (this.extraStatus2 != null)
		{
			errStr.append(this.extraStatus2 + " - ");
		}
		if (this.getMessage() != null)
		{
			errStr.append(this.getMessage() + " - ");
		}
		if (this.origException != null && this.origException.toString() != null)
		{
			errStr.append(this.origException.toString());
		}
		if (errStr.toString().endsWith(" - "))
		{
			return errStr.toString().substring(0, errStr.length() - 3);
		}
		else
		{
			return errStr.toString();
		}
	}
}
