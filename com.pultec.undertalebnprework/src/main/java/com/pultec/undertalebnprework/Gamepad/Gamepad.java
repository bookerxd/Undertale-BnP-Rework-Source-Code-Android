package com.pultec.undertalebnprework;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.os.ParcelUuid;
import android.os.Build;
import android.os.Build.VERSION;

import java.lang.IllegalAccessException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.content.pm.PackageManager;
import com.yoyogames.runner.RunnerJNILib;
import android.annotation.SuppressLint;

// --------------------------------------------------------------------------------------------
// --------------------------------------------------------------------------------------------
// base class for GamepadHandler
class GamepadHandler
{
	public boolean HandleMotionEvent( MotionEvent _event ) {
		return false;
	} // end HandleMotionEvent

	public boolean HandleKeyEvent( int _id, KeyEvent _event ) {
		return false;
	} // end HandleKeyEvent

	public void PollInputDevices() {
	} // end PollInputDevices
} // end GamepadHandler

// --------------------------------------------------------------------------------------------
// --------------------------------------------------------------------------------------------
// Gamepad Handler for API12
class GamepadHandler_API12 extends GamepadHandler
{
	static class GamepadInstance
	{
		public int idDevice;
		public String name;
		public String desc;
		public int vendorId;
		public int productId;
		public int buttonMask;
		public ArrayList<InputDevice.MotionRange> axes;
		public ArrayList<InputDevice.MotionRange> hats;
	} // GamepadInstance

    static class RangeComparator implements Comparator<InputDevice.MotionRange> {
        @Override
        public int compare(InputDevice.MotionRange arg0, InputDevice.MotionRange arg1) {
            // Some controllers, like the Moga Pro 2, return AXIS_GAS (22) for right trigger and AXIS_BRAKE (23) for left trigger - swap them so they're sorted in the right order for SDL
            int arg0Axis = arg0.getAxis();
            int arg1Axis = arg1.getAxis();
            if (arg0Axis == MotionEvent.AXIS_GAS) {
                arg0Axis = MotionEvent.AXIS_BRAKE;
            } else if (arg0Axis == MotionEvent.AXIS_BRAKE) {
                arg0Axis = MotionEvent.AXIS_GAS;
            }
            if (arg1Axis == MotionEvent.AXIS_GAS) {
                arg1Axis = MotionEvent.AXIS_BRAKE;
            } else if (arg1Axis == MotionEvent.AXIS_BRAKE) {
                arg1Axis = MotionEvent.AXIS_GAS;
            }

            return arg0Axis - arg1Axis;
        }
    } // end RangeComparator

	private ArrayList<GamepadInstance> m_Gamepads;

	// --------------------------------------------------------------------
	// --------------------------------------------------------------------
	public GamepadHandler_API12() {
		m_Gamepads = new ArrayList<GamepadInstance>();
	} // end constructor

	// --------------------------------------------------------------------
	// --------------------------------------------------------------------
    public String GetJoystickDescriptor(InputDevice _joystickDevice) {
        return _joystickDevice.getName();
    } // end GetJoystickDescriptor

	// --------------------------------------------------------------------
	// --------------------------------------------------------------------
    public int GetProductId(InputDevice _joystickDevice) {
        return 0;
    } // end GetProductId

	// --------------------------------------------------------------------
	// --------------------------------------------------------------------
    public int GetVendorId(InputDevice _joystickDevice) {
        return 0;
    } // end GetVendorId

	// --------------------------------------------------------------------
	// --------------------------------------------------------------------
    public int GetButtonMask(InputDevice _joystickDevice) {
        return 0;
    } // end GetButtonMask

	// --------------------------------------------------------------------
	// --------------------------------------------------------------------
	public GamepadInstance GetGamepad( int _id )
	{
		for( int i=0; i<m_Gamepads.size(); ++i) {
			GamepadInstance gi = m_Gamepads.get(i);
			if (gi.idDevice == _id) {
				return gi;
			} // end if
		} // end for

		return null;
	} // end GetGamepad

	// --------------------------------------------------------------------
	// --------------------------------------------------------------------
	@SuppressLint("MissingPermission")
	@Override
	public void PollInputDevices()
	{	
        int[] deviceIds = InputDevice.getDeviceIds();
        for(int i=0; i < deviceIds.length; ++i) {
			int id = deviceIds[i];
			if (id < 0) continue;

			// check to see if we already have this device id
			GamepadInstance gi = GetGamepad(id);
			if (gi != null) continue;

            InputDevice joystickDevice = InputDevice.getDevice(id);
			int deviceSources = joystickDevice.getSources();
			if (((deviceSources & InputDevice.SOURCE_CLASS_JOYSTICK) == InputDevice.SOURCE_CLASS_JOYSTICK) ||
				((deviceSources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) ||
				((deviceSources & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD)) {

					gi = new GamepadInstance();
                    List<InputDevice.MotionRange> ranges = joystickDevice.getMotionRanges();
                    Collections.sort(ranges, new RangeComparator());
					gi.idDevice = id;
					gi.name = joystickDevice.getName();
					gi.desc = GetJoystickDescriptor(joystickDevice);
					gi.axes = new ArrayList<InputDevice.MotionRange>();
					gi.hats = new ArrayList<InputDevice.MotionRange>();
					gi.vendorId = GetVendorId(joystickDevice);
					gi.productId = GetProductId(joystickDevice);
					gi.buttonMask = GetButtonMask(joystickDevice);
					
                    for (InputDevice.MotionRange range : ranges ) {
                        if ((range.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) != 0) {
                            if (range.getAxis() == MotionEvent.AXIS_HAT_X ||
                                range.getAxis() == MotionEvent.AXIS_HAT_Y) {
								gi.hats.add( range );
                            }
                            else {
								gi.axes.add( range );
                            } // end else
                        } // end if
                    } // end for

					m_Gamepads.add(gi);
					// do the native call here
					RunnerJNILib.onGPDeviceAdded( gi.idDevice, gi.name, gi.desc, gi.productId, gi.vendorId, gi.axes.size(), gi.hats.size()/2, gi.buttonMask );
					Log.i("yoyo", "GAMEPAD :: found device id:" + deviceIds[i] + 
											" name:" + gi.name + 
											" desc:" + gi.desc + 
											" productId:" + gi.productId + 
											" vendorId:" + gi.vendorId +
											" maskButtons:" + Integer.toHexString(gi.buttonMask) + 
											" numHats:" + gi.hats.size()/2 + 
											" numAxes:" + gi.axes.size()
											);
			} // end if
        } // end for

		// check for removed devices
		ArrayList<Integer> removedDevices = new ArrayList<Integer>();
		for( int i=0; i<m_Gamepads.size(); ++i) {
			GamepadInstance gi = m_Gamepads.get(i);
			int j;
			for( j=0; j<deviceIds.length;++ j) {
				if (gi.idDevice == deviceIds[j]) break;
			} // end for

			if (j == deviceIds.length) {
				removedDevices.add( gi.idDevice );
				Log.i("yoyo", "GAMEPAD :: removed device id:" +  gi.idDevice  + 
										" name:" + gi.name + 
										" desc:" + gi.desc + 
										" productId:" + gi.productId + 
										" vendorId:" + gi.vendorId +
										" maskButtons:" + Integer.toHexString(gi.buttonMask) + 
										" numHats:" + gi.hats.size()/2 + 
										" numAxes:" + gi.axes.size()
										);
			} // end if
		} // end for

		// process any removed devices
		for( int i=0; i<removedDevices.size() ; ++i) {
			int id = removedDevices.get(i).intValue();
			// do the native call here
			RunnerJNILib.onGPDeviceRemoved( id );

			// remove from the list of gamepads
			for( int j=0; j<m_Gamepads.size(); ++j) {
				if (m_Gamepads.get(j).idDevice == id) {
					m_Gamepads.remove(j);
					break;
				} // end if
			} // end for
		} // end for
		
		Log.i("yoyo", "GAMEPAD: Enumeration complete");
	} // end PollInputDevices

	// --------------------------------------------------------------------
	// --------------------------------------------------------------------
	@SuppressLint("MissingPermission")
	@Override
	public boolean HandleMotionEvent( MotionEvent _event ) {
		if ((_event.getSource() & InputDevice.SOURCE_JOYSTICK) != 0) {
			int actionPointerIndex = _event.getActionIndex();
			int action = _event.getActionMasked();
			switch( action ) {
			case MotionEvent.ACTION_MOVE:
				GamepadInstance gi = GetGamepad(_event.getDeviceId());
				if (gi != null) {
					for( int i=0; i<gi.axes.size(); ++i) {
						InputDevice.MotionRange range = gi.axes.get(i);
						float value = (2.0f * ((_event.getAxisValue( range.getAxis(), actionPointerIndex) - range.getMin()) / range.getRange())) - 1.0f;
						// send this to the native code
						RunnerJNILib.onGPNativeAxis( gi.idDevice, i, value );
					} // end for

					for( int i=0; i<gi.hats.size(); i+=2) {
						InputDevice.MotionRange hat1 = gi.hats.get(i);
						InputDevice.MotionRange hat2 = gi.hats.get(i+1);
						int hatX = Math.round( _event.getAxisValue( hat1.getAxis(), actionPointerIndex) );
						int hatY = Math.round( _event.getAxisValue( hat2.getAxis(), actionPointerIndex) );
						// send this to the native code
						RunnerJNILib.onGPNativeHat( gi.idDevice, i/2, hatX, hatY );
					} // end for
				} // end if
				break;
			default:
				break;
			} // end switch
		} // end if
		return true;
	} // end HandleMotionEvent

	// --------------------------------------------------------------------
	// --------------------------------------------------------------------
	@SuppressLint("MissingPermission")
	@Override
	public boolean HandleKeyEvent( int _id, KeyEvent _event ) {
		boolean ret = false;
		GamepadInstance gi = GetGamepad(_id);
		if (gi != null) {
			int action = _event.getAction();
			if (action == KeyEvent.ACTION_DOWN) {
				RunnerJNILib.onGPKeyDown( gi.idDevice, _event.getKeyCode() );
				ret = true;
			} // end if
			else if (action == KeyEvent.ACTION_UP) {
				RunnerJNILib.onGPKeyUp( gi.idDevice, _event.getKeyCode() );
				ret = true;
			} // end if
		} // end if
		return ret;
	} // end HandleKeyEvent

} // end GamepadHandler_API12

// --------------------------------------------------------------------------------------------
// --------------------------------------------------------------------------------------------
class GamepadHandler_API16 extends GamepadHandler_API12
{
	// --------------------------------------------------------------------
	// --------------------------------------------------------------------
	@Override
    public String GetJoystickDescriptor(InputDevice _joystickDevice) {
		String desc = _joystickDevice.getDescriptor();
		if ((desc != null) && !desc.isEmpty()) {
			return desc;
		} // end if

        return super.GetJoystickDescriptor(_joystickDevice);
    } // end GetJoystickDescriptor
} // end GamepadHandler_API16

// --------------------------------------------------------------------------------------------
// --------------------------------------------------------------------------------------------
class GamepadHandler_API19 extends GamepadHandler_API16
{
	// --------------------------------------------------------------------
	// --------------------------------------------------------------------
	@Override
    public int GetProductId(InputDevice _joystickDevice) {
        return _joystickDevice.getProductId();
    } // end GetProductId

	// --------------------------------------------------------------------
	// --------------------------------------------------------------------
	@Override
    public int GetVendorId(InputDevice _joystickDevice) {
        return _joystickDevice.getVendorId();
    } // end GetVendorId

    @Override
    public int GetButtonMask(InputDevice joystickDevice) {
        int button_mask = 0;
        int[] keys = new int[] {
            KeyEvent.KEYCODE_BUTTON_A,
            KeyEvent.KEYCODE_BUTTON_B,
            KeyEvent.KEYCODE_BUTTON_X,
            KeyEvent.KEYCODE_BUTTON_Y,
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_BUTTON_MODE,
            KeyEvent.KEYCODE_BUTTON_START,
            KeyEvent.KEYCODE_BUTTON_THUMBL,
            KeyEvent.KEYCODE_BUTTON_THUMBR,
            KeyEvent.KEYCODE_BUTTON_L1,
            KeyEvent.KEYCODE_BUTTON_R1,
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_BUTTON_SELECT,
            KeyEvent.KEYCODE_DPAD_CENTER,

            KeyEvent.KEYCODE_BUTTON_L2,
            KeyEvent.KEYCODE_BUTTON_R2,
            KeyEvent.KEYCODE_BUTTON_C,
            KeyEvent.KEYCODE_BUTTON_Z,
            KeyEvent.KEYCODE_BUTTON_1,
            KeyEvent.KEYCODE_BUTTON_2,
            KeyEvent.KEYCODE_BUTTON_3,
            KeyEvent.KEYCODE_BUTTON_4,
            KeyEvent.KEYCODE_BUTTON_5,
            KeyEvent.KEYCODE_BUTTON_6,
            KeyEvent.KEYCODE_BUTTON_7,
            KeyEvent.KEYCODE_BUTTON_8,
            KeyEvent.KEYCODE_BUTTON_9,
            KeyEvent.KEYCODE_BUTTON_10,
            KeyEvent.KEYCODE_BUTTON_11,
            KeyEvent.KEYCODE_BUTTON_12,
            KeyEvent.KEYCODE_BUTTON_13,
            KeyEvent.KEYCODE_BUTTON_14,
            KeyEvent.KEYCODE_BUTTON_15,
            KeyEvent.KEYCODE_BUTTON_16,
        };
        int[] masks = new int[] {
            (1 << 0),   // A -> A
            (1 << 1),   // B -> B
            (1 << 2),   // X -> X
            (1 << 3),   // Y -> Y
            (1 << 4),   // BACK -> BACK
            (1 << 5),   // MODE -> GUIDE
            (1 << 6),   // START -> START
            (1 << 7),   // THUMBL -> LEFTSTICK
            (1 << 8),   // THUMBR -> RIGHTSTICK
            (1 << 9),   // L1 -> LEFTSHOULDER
            (1 << 10),  // R1 -> RIGHTSHOULDER
            (1 << 11),  // DPAD_UP -> DPAD_UP
            (1 << 12),  // DPAD_DOWN -> DPAD_DOWN
            (1 << 13),  // DPAD_LEFT -> DPAD_LEFT
            (1 << 14),  // DPAD_RIGHT -> DPAD_RIGHT
            (1 << 4),   // SELECT -> BACK
            (1 << 0),   // DPAD_CENTER -> A
            (1 << 15),  // L2 -> ??
            (1 << 16),  // R2 -> ??
            (1 << 17),  // C -> ??
            (1 << 18),  // Z -> ??
            (1 << 20),  // 1 -> ??
            (1 << 21),  // 2 -> ??
            (1 << 22),  // 3 -> ??
            (1 << 23),  // 4 -> ??
            (1 << 24),  // 5 -> ??
            (1 << 25),  // 6 -> ??
            (1 << 26),  // 7 -> ??
            (1 << 27),  // 8 -> ??
            (1 << 28),  // 9 -> ??
            (1 << 29),  // 10 -> ??
            (1 << 30),  // 11 -> ??
            (1 << 31),  // 12 -> ??
            // We're out of room...
            0xFFFFFFFF,  // 13 -> ??
            0xFFFFFFFF,  // 14 -> ??
            0xFFFFFFFF,  // 15 -> ??
            0xFFFFFFFF,  // 16 -> ??
        };
        boolean[] has_keys = joystickDevice.hasKeys(keys);
        for (int i = 0; i < keys.length; ++i) {
            if (has_keys[i]) {
                button_mask |= masks[i];
            }
        }
        return button_mask;
    } // end GetButtonMask
} // end GamepadHandler_API19

// --------------------------------------------------------------------------------------------
// --------------------------------------------------------------------------------------------
public class Gamepad
{	
	protected static GamepadHandler ms_GamepadHandler;

	public static void Initialise() {
		if (Build.VERSION.SDK_INT >= 19) {
			ms_GamepadHandler = new GamepadHandler_API19();
		} // end if
		else if (Build.VERSION.SDK_INT >= 16) {
			ms_GamepadHandler = new GamepadHandler_API16();
		} // end if
		else if (Build.VERSION.SDK_INT >= 12) {
			ms_GamepadHandler = new GamepadHandler_API12();
		} // end if
		else {
			ms_GamepadHandler = new GamepadHandler();
		} // end else
	} // end Initialise

	
	/**
	 * Performed during the Application's onStart/onResume
	 */
	public static void EnumerateDevices()
	{	
		if (ms_GamepadHandler == null) {
			Initialise();
		} // end if
		ms_GamepadHandler.PollInputDevices();
	}

	/**
	 * Respond to dispatchKeyEvent() from RunnerActivity
	 */
	static public void handleKeyEvent(KeyEvent ev)
	{		
		//Log.i("yoyo", "KEYEVENT: " + ev.getKeyCode() + " " + ev.keyCodeToString(ev.getKeyCode()) + " " + Integer.toHexString(ev.getFlags()) + " " + System.currentTimeMillis());
		int deviceId = ev.getDeviceId();
		InputDevice device = InputDevice.getDevice(deviceId);
		if ((device != null) && (deviceId >= 0)) {
			int deviceSources = device.getSources();
			if (((deviceSources & InputDevice.SOURCE_CLASS_JOYSTICK) == InputDevice.SOURCE_CLASS_JOYSTICK) ||
				((deviceSources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) ||
				((deviceSources & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD)) {
				ms_GamepadHandler.HandleKeyEvent(deviceId, ev);
			} // end if
		} // end if

	}
		
	//------------------------------------------------------------------------------------------------	
	//------------------------------------------------------------------------------------------------	
	
	/**
	 * Respond to dispatchGenericMotionEvent() from RunnerActivity
	 */
	static public void handleMotionEvent(MotionEvent ev)
	{		
		//Log.i("yoyo", "MOTIONEVENT: " + ev.toString() );
		switch( ev.getSource() ) {
		case InputDevice.SOURCE_JOYSTICK:
		case InputDevice.SOURCE_GAMEPAD:
		case InputDevice.SOURCE_DPAD:
			ms_GamepadHandler.HandleMotionEvent(ev);
			break;
		} // end switch

	}

}
