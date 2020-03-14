// @java.file.header

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.apache.nlpcraft.model;

import java.util.*;

/**
 * Descriptor of the user. Returned from {@link NCRequest#getUser()} method.
 *
 * @see NCRequest
 */
public interface NCUser {
    /**
     * Gets ID of this user.
     * 
     * @return User ID.
     */
    long getId();

    /**
     * Gets first name of the user.
     *
     * @return First name of the user.
     */
    Optional<String> getFirstName();

    /**
     * Gets last name of the user.
     *
     * @return Last name of the user.
     */
    Optional<String> getLastName();

    /**
     * Gets properties associated with the user.
     *
     * @return Optional map of properties associated with the user.
     */
    Optional<Map<String, String>> getProperties();

    /**
     * Gets email of the user.
     *
     * @return Email of the user.
     */
    Optional<String> getEmail();

    /**
     * Gets optional user avatar URL ({@code data:} or {@code http:} scheme URLs).
     *
     * @return User avatar URL ({@code data:} or {@code http:} scheme URLs).
     */
    Optional<String> getAvatarUrl();

    /**
     * Tests whether or not the user has administrative privileges.
     *
     * @return Whether or not the user has administrative privileges.
     */
    boolean isAdmin();

    /**
     * Gets signup timestamp of the user.
     *
     * @return Signup timestamp of the user.
     */
    long getSignupTimestamp();
}
