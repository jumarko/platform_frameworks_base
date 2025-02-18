/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.media;

import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

/**
 * @hide
 * Class to represent device type (speaker, headset...), address and role (input, output)
 * of an audio device.
 * <p>Unlike {@link AudioDeviceInfo}, the device
 * doesn't need to be connected to be uniquely identified, it can
 * for instance represent a specific A2DP headset even after a
 * disconnection, whereas the corresponding <code>AudioDeviceInfo</code>
 * would then be invalid.
 * <p>While creating / obtaining an instance is not protected by a
 * permission, APIs using one rely on MODIFY_AUDIO_ROUTING.
 */
@SystemApi
public final class AudioDeviceAddress implements Parcelable {

    /**
     * A role identifying input devices, such as microphones.
     */
    public static final int ROLE_INPUT = AudioPort.ROLE_SOURCE;
    /**
     * A role identifying output devices, such as speakers or headphones.
     */
    public static final int ROLE_OUTPUT = AudioPort.ROLE_SINK;

    /** @hide */
    @IntDef(flag = false, prefix = "ROLE_", value = {
            ROLE_INPUT, ROLE_OUTPUT }
    )
    @Retention(RetentionPolicy.SOURCE)
    public @interface Role {}

    /**
     * The audio device type, as defined in {@link AudioDeviceInfo}
     */
    private final @AudioDeviceInfo.AudioDeviceType int mType;
    /**
     * The unique address of the device. Some devices don't have addresses, only an empty string.
     */
    private final @NonNull String mAddress;

    /**
     * Is input or output device
     */
    private final @Role int mRole;

    /**
     * Constructor from a valid {@link AudioDeviceInfo}
     * @param deviceInfo the connected audio device from which to obtain the device-identifying
     *                   type and address.
     */
    public AudioDeviceAddress(@NonNull AudioDeviceInfo deviceInfo) {
        Objects.requireNonNull(deviceInfo);
        mRole = deviceInfo.isSink() ? ROLE_OUTPUT : ROLE_INPUT;
        mType = deviceInfo.getType();
        mAddress = deviceInfo.getAddress();
    }

    public AudioDeviceAddress(@Role int role, @AudioDeviceInfo.AudioDeviceType int type,
                              @NonNull String address) {
        Objects.requireNonNull(address);
        if (role != ROLE_OUTPUT && role != ROLE_INPUT) {
            throw new IllegalArgumentException("Invalid role " + role);
        }
        if (role == ROLE_OUTPUT && !AudioDeviceInfo.isValidAudioDeviceTypeOut(type)) {
            throw new IllegalArgumentException("Invalid output device type " + type);
        }
        if (role == ROLE_INPUT && !AudioDeviceInfo.isValidAudioDeviceTypeIn(type)) {
            throw new IllegalArgumentException("Invalid input device type " + type);
        }

        mRole = role;
        mType = type;
        mAddress = address;
    }

    public @Role int getRole() {
        return mRole;
    }

    public @AudioDeviceInfo.AudioDeviceType int getType() {
        return mType;
    }

    public @NonNull String getAddress() {
        return mAddress;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mRole, mType, mAddress);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AudioDeviceAddress that = (AudioDeviceAddress) o;
        return ((mRole == that.mRole)
                && (mType == that.mType)
                && mAddress.equals(that.mAddress));
    }

    /** @hide */
    public static String roleToString(@Role int role) {
        return (role == ROLE_OUTPUT ? "output" : "input");
    }

    @Override
    public String toString() {
        return new String("AudioDeviceAddress:"
                + " role:" + roleToString(mRole)
                + " type:" + (mRole == ROLE_OUTPUT ? AudioSystem.getOutputDeviceName(
                        AudioDeviceInfo.convertDeviceTypeToInternalDevice(mType))
                        : AudioSystem.getInputDeviceName(
                                AudioDeviceInfo.convertDeviceTypeToInternalDevice(mType)))
                + " addr:" + mAddress);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mRole);
        dest.writeInt(mType);
        dest.writeString(mAddress);
    }

    private AudioDeviceAddress(@NonNull Parcel in) {
        mRole = in.readInt();
        mType = in.readInt();
        mAddress = in.readString();
    }

    public static final @NonNull Parcelable.Creator<AudioDeviceAddress> CREATOR =
            new Parcelable.Creator<AudioDeviceAddress>() {
        /**
         * Rebuilds an AudioDeviceAddress previously stored with writeToParcel().
         * @param p Parcel object to read the AudioDeviceAddress from
         * @return a new AudioDeviceAddress created from the data in the parcel
         */
        public AudioDeviceAddress createFromParcel(Parcel p) {
            return new AudioDeviceAddress(p);
        }

        public AudioDeviceAddress[] newArray(int size) {
            return new AudioDeviceAddress[size];
        }
    };
}
