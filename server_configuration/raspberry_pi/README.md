# SmartHomeHub
Code for Raspberry Pi ZigBee MQTT Server configuration files.

# Zigbee2MQTT Setup Guide for Raspberry Pi with Docker

## Overview
Zigbee2MQTT allows you to control Zigbee devices via MQTT, providing a bridge between Zigbee devices and your home automation system. This guide outlines the steps to install and configure Zigbee2MQTT using Docker on a Linux system.

---

## Prerequisites
Before proceeding, ensure you have the following:

- A Raspberry Pi machine.
- A Zigbee USB coordinator (e.g., Sonoff Zigbee 3.0 Dongle Plus or ZBDongle-E).
- Power cable and power supply and SD card with Raspberry Pi OS installed.
- Docker and Docker Compose installed.

---

## Installation Steps

### 1. Install Required Software

#### Install Docker
1. Update your system:
  ```bash
  sudo apt update
  sudo apt upgrade -y
  ```
2. Install Docker:
  ```bash
  sudo apt install -y docker.io
  ```
3. Start and enable Docker:
  ```bash
  sudo systemctl start docker
  sudo systemctl enable docker
  ```
4. Verify the installation:
  ```bash
  docker --version
  ```

#### Install Docker Compose
1. Download Docker Compose:
  ```bash
  sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
  ```
2. Make it executable:
  ```bash
  sudo chmod +x /usr/local/bin/docker-compose
  ```
3. Verify the installation:
  ```bash
  docker-compose --version
  ```

### 2. Set Up Zigbee2MQTT with Docker

#### Create a Directory for Zigbee2MQTT
1. Create a directory for the Zigbee2MQTT configuration:
  ```bash
  mkdir zigbee2mqtt
  cd zigbee2mqtt
  ```
2. Create a docker-compose.yml file
Inside the zigbee2mqtt directory, create a docker-compose.yml file:
  ```bash
  nano docker-compose.yml
  ```
3. Add the configuration from docker-compose.yaml file

#### Configure Zigbee2MQTT
1. Create a data directory for the Zigbee2MQTT configuration:
  ```bash
  mkdir data
  ```
2. Inside the data directory, create a configuration.yaml file:
  ```bash
  nano data/configuration.yaml
  ```
3. Add the configuration from configuration.yaml file

#### Connecting and configuring the Zigbee adapter
1. Connect your Zigbee adapter to the Raspberry Pi USB port.
2. Check which port the device is using:
  ```bash
  ls /dev/serial/by-id
  ```
  Example of answer:
  ```bash
  /dev/serial/by-id/usb-ITead_Sonoff_Zigbee_3.0_USB_Dongle_Plus
  ```

### 3. Start Zigbee2MQTT

1. Navigate back to the zigbee2mqtt directory:
  ```bash
  cd ..
  ```
2. Start Zigbee2MQTT using Docker Compose:
  ```bash
  sudo docker-compose up -d
  ```
3. Check the logs to ensure everything is running smoothly:
  ```bash
  sudo docker-compose logs -f
  ```
---

## Common Issues and Troubleshooting

### Zigbee Coordinator Not Detected
- Ensure the correct USB device path is specified in configuration.yaml. Use dmesg | grep tty to confirm the correct path.
- Ensure the user running Docker has access to the USB device. Add your user to the dialout group:
  ```bash
  sudo usermod -aG dialout $USER
  ```
  
### MQTT Connection Issues
- Verify that the MQTT broker is running and accessible.
- Check the server field in configuration.yaml for the correct broker URL.

### Zigbee Devices Not Pairing
- Ensure devices are in pairing mode.
- Restart Zigbee2MQTT and try pairing again.
- Check that permit_join is enabled in configuration.yaml:
  ```yaml
  permit_join: true
  ```

  ### Additional Commands
- Stop Zigbee2MQTT:
  ```bash
  sudo docker-compose down
  ```
- Update Zigbee2MQTT
Pull the latest Docker image:
  ```bash
  sudo docker pull koenkk/zigbee2mqtt
  ```
- Restart the container:
  ```bash
  sudo docker-compose up -d
  ```
- View Zigbee2MQTT Logs:
  ```bash
  sudo docker-compose logs -f
  ```

### General Debugging Tips
- Use a powerful power supply suitable for the Raspberry Pi, as well as additional adapters and cables to connect the ZigBee equipment.
- If you are using a not very powerful version of Rasbery Pie, such as 3B+, you may experience system freezes when turning on the system. You can either add an external additional power source for the ZigBee stick, or connect it after Rasbery Pie is fully turned on.

---

## Additional Resources
- [Zigbee2MQTT Documentation](https://www.zigbee2mqtt.io/)
- [Docker Documentation](https://docs.docker.com/engine/install/)
  
---

## Acknowledgments
This guide is based on the official Zigbee2MQTT setup instructions and adapted for Raspberry Pi with Docker.
