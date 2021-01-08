/*-
 * ========================LICENSE_START=================================
 * restheart-security
 * %%
 * Copyright (C) 2018 - 2020 SoftInstigate
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * =========================LICENSE_END==================================
 */

package org.restheart.security.plugins.authorizers;

import static org.restheart.plugins.ConfigurablePlugin.argValue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.bson.BsonDocument;
import org.restheart.ConfigurationException;

/**
 * This holdes the general permissions for MongoService
 */
public class MongoPermissions {
    final boolean whitelistManagementRequests;
    final boolean whitelistBulkPatch;
    final boolean whitelistBulkDelete;
    final boolean allowAllWriteModes;

    final Set<String> hiddenProps = Sets.newHashSet();
    final Set<String> protectedProps = Sets.newHashSet();
    final Map<String, String> overriddenProps = Maps.newHashMap();

    public MongoPermissions() {
        this.whitelistManagementRequests = false;
        this.whitelistBulkPatch = false;
        this.whitelistBulkDelete = false;
        this.allowAllWriteModes = false;
    }

    MongoPermissions(boolean whitelistManagementRequests, boolean whitelistBulkPatch, boolean whitelistBulkDelete,
            boolean allowAllWriteModes, Set<String> hiddenProps, Set<String> protectedProps, Map<String, String> overriddenProps) {
        this.whitelistManagementRequests = whitelistManagementRequests;
        this.whitelistBulkPatch = whitelistBulkPatch;
        this.whitelistBulkDelete = whitelistBulkDelete;
        this.allowAllWriteModes = allowAllWriteModes;
        if (hiddenProps != null) {
            this.hiddenProps.addAll(hiddenProps);
        }

        if (protectedProps != null) {
            this.protectedProps.addAll(protectedProps);
        }

        if (overriddenProps != null) {
            this.overriddenProps.putAll(overriddenProps);
        }
    }

    public static MongoPermissions from(BsonDocument args) throws ConfigurationException {
        if (args == null || args.isEmpty()) {
            // return default values
            return new MongoPermissions();
        } else {
            return new MongoPermissions(
                parseBooleanArg(args, "whitelistManagementRequests"),
                parseBooleanArg(args, "whitelistBulkPatch"),
                parseBooleanArg(args, "whitelistBulkDelete"),
                parseBooleanArg(args, "allowAllWriteModes"),
                parseSetArg(args, "hiddenProps"),
                parseSetArg(args, "protectedProps"),
                null);
        }
    }

    public static MongoPermissions from(Map<String, Object> args) throws ConfigurationException {
        if (args == null || args.isEmpty()) {
            // return default values
            return new MongoPermissions();
        } else {
            return new MongoPermissions(
                parseBooleanArg(args, "whitelistManagementRequests"),
                parseBooleanArg(args, "whitelistBulkPatch"),
                parseBooleanArg(args, "whitelistBulkDelete"),
                parseBooleanArg(args, "allowAllWriteModes"),
                parseSetArg(args, "hiddenProps"),
                parseSetArg(args, "protectedProps"),
                null);
        }
    }

    private static boolean parseBooleanArg(Map<String, Object> args, String key) throws ConfigurationException {
        if (args.containsKey(key)) {
            Object _value = argValue(args, key);

            if (_value != null && _value instanceof Boolean) {
                return (Boolean) _value;
            } else {
                throw new ConfigurationException("Wrong permission: mongo." + key + " must be a boolean");
            }
        } else {
            return true;
        }
    }

    private static Set<String> parseSetArg(Map<String, Object> args, String key) throws ConfigurationException {
        if (args.containsKey(key)) {
            Object _value = argValue(args, key);

            if (_value != null && _value instanceof List<?>) {
                HashSet<String> ret = Sets.newHashSet();;
                List<?> _set = (List<?>) _value;

                for (var _entry: _set) {
                    if (_entry instanceof String) {
                        ret.add((String)_entry);
                    } else {
                        throw new ConfigurationException("Wrong permission: mongo." + key + " must be a list of strings");
                    }
                }
                return ret;
            } else {
                throw new ConfigurationException("Wrong permission: mongo." + key + " must be a list of strings");
            }
        } else {
            return Sets.newHashSet();
        }
    }

    private static boolean parseBooleanArg(BsonDocument args, String key) throws ConfigurationException {
        if (args.containsKey(key)) {
            var _value = args.get(key);

            if (_value != null && _value.isBoolean()) {
                return _value.asBoolean().getValue();
            } else {
                throw new ConfigurationException("Wrong permission: mongo." + key + " must be a boolean");
            }
        } else {
            return true;
        }
    }

    private static Set<String> parseSetArg(BsonDocument args, String key) throws ConfigurationException {
        if (args.containsKey(key)) {
            var _value = args.get(key);

            if (_value != null && _value.isArray()) {
                HashSet<String> ret = Sets.newHashSet();;
                var _array = _value.asArray();

                for (var _entry: _array) {
                    if (_entry != null && _entry.isString()) {
                        ret.add(_entry.asString().getValue());
                    } else {
                        throw new ConfigurationException("Wrong permission: mongo." + key + " must be an array of strings");
                    }
                }
                return ret;
            } else {
                throw new ConfigurationException("Wrong permission: mongo." + key + " must be an array of strings");
            }
        } else {
            return Sets.newHashSet();
        }
    }

    public boolean getWhitelistManagementRequests() {
        return this.whitelistManagementRequests;
    }

    public boolean isWhitelistManagementRequests() {
        return this.whitelistManagementRequests;
    }

    public boolean getWhitelistBulkPatch() {
        return this.whitelistBulkPatch;
    }

    public boolean isWhitelistBulkPatch() {
        return this.whitelistBulkPatch;
    }

    public boolean getWhitelistBulkDelete() {
        return this.whitelistBulkDelete;
    }

    public boolean isWhitelistBulkDelete() {
        return this.whitelistBulkDelete;
    }

    public boolean getAllowAllWriteModes() {
        return this.allowAllWriteModes;
    }

    public boolean isAllowAllWriteModes() {
        return this.allowAllWriteModes;
    }

    public Set<String> getHiddenProps() {
        return this.hiddenProps;
    }

    public Set<String> getProtectedProps() {
        return this.protectedProps;
    }

    public Map<String,String> getOverriddenProps() {
        return this.overriddenProps;
    }
}
