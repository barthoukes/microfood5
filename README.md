# Project Overview

This repository contains the development of a modern, tablet-based Point-of-Sale (POS) client, written in Kotlin for Android. It is designed to function as a remote terminal within a larger POS ecosystem, communicating via gRPC with a central server.

![Example Microfood5](https://www.kassaku.nl/wp-content/uploads/2025/10/Frida-app-1024x566.jpg)

This Kotlin application represents the next evolution of our proven POS system. While this specific Kotlin client is in active development, the core C++ POS software that powers it is already successfully deployed and operational in numerous restaurants across the Netherlands.
Architecture & Purpose

The core philosophy is a language-agnostic architecture where the business logic resides in a central server (our battle-tested C++ application), and the user interface can be implemented in any technology that supports gRPC.
text

[Kotlin Tablet UI] <--gRPC--> [C++ POS Server Core] <---> [Hardware Peripherals]

This project serves as a robust example for:

    Building modern, reactive Android layouts for a POS workflow

    Implementing a full Point-of-Sale interface (order entry, modifications, payment)

    Using gRPC as a high-performance communication layer between different programming languages (C++ and Kotlin)

# Getting Started for Developers
## Prerequisites

    Kotlin & Android Studio

    The .proto files from this repository (or the main project)

# Key Integration Point: The Proto Files

The protobuf files define the contract between the client and server. They are the single source of truth for all communication.

To build your own client in any language (JavaScript, Python, Go, etc.):

    Use the provided .proto files to generate the client code for your language

    Implement the UI logic that calls the generated gRPC methods

This ensures seamless and type-safe interoperability with our POS server core.
Current Status & Roadmap

    Status: In Active Development

        This Kotlin client is currently being built and refined on a local development machine

    Stable Foundation: The C++ server software it connects to is a mature product, running reliably in the real world

    Future Goal: When this Kotlin tablet app reaches maturity, it will be deployed alongside our existing C++ systems in our partner restaurants, offering them a sleek, modern table-side ordering experience

# About Kassaku – Your POS Partner

![Example working project](https://www.kassaku.nl/wp-content/uploads/2023/07/wok.jpg)

Kassaku (Indonesian for Kassa Aku = "My Cash Register") is built on over 15 years of experience in the hospitality industry, founded by Bart Houkes.
Our Story

In 1994, Bart began his journey in the POS world at a company specializing in solutions for Asian restaurants. After a successful career as a software architect, a chance encounter in 2010 with a restaurateur led to the founding of Houkes Horeca Applications. For over 15 years, we have been the POS specialist for hospitality businesses that value simplicity and quality.

Our systems are built on our core values: Love, Quality, Accountability, Trust, and Recognition. Whether you run a fast-service restaurant, a snack bar, or a cafeteria, our cash registers are designed to save time and reduce errors. No complicated manuals needed – your staff will work with it smoothly on the day of installation.

Interested in what our system can do for your business? Visit our website www.kassaku.nl to request a free, no-obligation demonstration!

This project is maintained by the development team at Kassaku / Houkes Horeca Applications.
