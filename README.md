# home-control-app-with-secure-communication
The goal of this application is to securely connect with QCA 402X devices. Application on the Board (Home Control Application ) is connected to end devices via GPIO (Smart Lock, Smoke Sensor) and a smart BLE Bulb. The communication between this Android application is encrypted using AES-128 Encryption algorithm and the same via Board application with the Smart Bulb.

During the initial connection with the Board from the application, it prompts user to input the Passkey to establish the connection.

Additional to the features, application maintains history of last connected Board information. This feature is to "Auto-Connect" with already paired Board. If last connected QCA board is already paired and is Active, application will establish the connection. Else application will initiate normal BLE scan process to discover the QCA402X boards and display the list.