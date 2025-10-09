package com.hha.resources

import com.google.protobuf.ByteString
import com.google.protobuf.kotlin.toByteStringUtf8
import com.hha.database.ConfigurationItem
import com.hha.database.ConfigurationItemList
import java.util.stream.IntStream.range

class Configuration {
    private var configurations: List<ConfigurationItem> = emptyList()

    /**
     * Sets the entire configuration list
     * @param configurations List of ConfigurationItems to set
     */
    fun setConfigurations(configurations: ConfigurationItemList)
    {
        this.configurations = emptyList()
        val size: Int = configurations.itemCount
        for (item in range(0, size))
        {
            val config = configurations.getItem(item)
            this.configurations += ConfigurationItem.newBuilder()
                .setConfiguration(config.configuration)
                .setValue(config.value)
                .build()
        }
    }

    /**
     * Finds a configuration by its key/name
     * @param name The configuration key to search for
     * @return The ConfigurationItem if found, null otherwise
     */
    fun findByName(name: String): ConfigurationItem?
    {
        return configurations.firstOrNull { it.configuration == name }
    }

    /**
     * Gets the value of a configuration by its name
     * @param name The configuration key
     * @return The value if found, null otherwise
     */
    fun getString(name: String): String
    {
        return findByName(name)?.value?.toStringUtf8() ?: ""
    }

    fun getOption(configuration: String): Boolean
    {
        val stringValue = getString(configuration)
        try {
            val nr = stringValue.toInt()
            return nr != 0
        } catch (e: NumberFormatException)
        {
            return false
        }
    }

    fun updateConfigurationValue(name: String, newValue: ByteString) {
        configurations = configurations.map { item ->
            if (item.configuration == name) {
                item.toBuilder().setValue(newValue).build()
            } else {
                item
            }
        }
    }

    fun setString(name: String, value: String) {
        updateConfigurationValue(name, value.toByteStringUtf8())
    }

    fun setValue(name: String, value: Int) {
        updateConfigurationValue(name, value.toString().toByteStringUtf8())
    }

    /**
     * Gets the value of a configuration by its name
     * @param name The configuration key
     * @return The value if found, null otherwise
     */
    fun getValue(name: String): Int
    {
        return findByName(name)?.value?.toStringUtf8()?.toIntOrNull() ?: 0
    }

    /**
     * Gets all configurations as a protobuf ConfigurationItemList
     * @return ConfigurationItemList containing all items
     */
    fun toConfigurationItemList(): ConfigurationItemList
    {
        return ConfigurationItemList.newBuilder()
            .addAllItem(configurations)
            .build()
    }

    /**
     * Gets all configurations as a list
     * @return List of all ConfigurationItems
     */
    fun getAll(): List<ConfigurationItem>
    {
        return configurations.toList()
    }

    /**
     * Checks if a configuration exists
     * @param name The configuration key to check
     * @return true if exists, false otherwise
     */
    fun contains(name: String): Boolean
    {
        return configurations.any { it.configuration == name }
    }

    /**
     * Gets the number of configurations
     * @return The count of configurations
     */
    fun size(): Int
    {
        return configurations.size
    }

    /**
     * Checks if there are no configurations
     * @return true if empty, false otherwise
     */
    fun isEmpty(): Boolean
    {
        return configurations.isEmpty()
    }

    /**
     * Updates or adds a configuration item
     * @param item The ConfigurationItem to update/add
     */
    fun update(item: ConfigurationItem)
    {
        val existing = findByName(item.configuration)
        configurations = if (existing != null)
        {
            configurations.map { if (it.configuration == item.configuration) item else it }
        } else
        {
            configurations + item
        }
    }

    fun getBackgroundColour(name: String): Int = getColour(name) or 0xff000000.toInt()

    fun getColour(name: String): Int
    {
        val str = getString(name)
        return when
        {
            str.startsWith("0x") || str.startsWith("0X") ->
            {
                str.substring(2).toIntOrNull(16) ?: 0
            }

            else -> str.toIntOrNull() ?: 0
        }
    }

    fun getTextColour(name: String) : Int = getValue(name) or 0xFF000000.toInt()

    /**
     * Removes a configuration by name
     * @param name The configuration key to remove
     * @return true if removed, false if not found
     */
    fun remove(name: String): Boolean
    {
        return if (contains(name))
        {
            configurations = configurations.filter { it.configuration != name }
            true
        } else
        {
            false
        }
    }

}