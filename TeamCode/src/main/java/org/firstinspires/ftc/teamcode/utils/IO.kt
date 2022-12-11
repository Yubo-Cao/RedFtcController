package org.firstinspires.ftc.teamcode.utils

import java.net.ServerSocket
import java.net.Socket

fun send(ip: String, port: Int, message: java.io.Serializable) {
    Socket(ip, port).use { socket ->
        socket.getOutputStream().use { stream ->
            java.io.ObjectOutputStream(stream).use { oos ->
                oos.writeObject(message)
            }
        }
    }
}

fun receive(port: Int): Any {
    ServerSocket(port).use { server ->
        server.accept().use { socket ->
            socket.getInputStream().use { stream ->
                java.io.ObjectInputStream(stream).use { ois ->
                    return ois.readObject()
                }
            }
        }
    }
}