## **Microfood5: Building a Modern, Tablet-First POS for the Agile Restaurant**

In the bustling environment of a restaurant—the sizzle of the wok, the clatter of plates, the constant flow of customers—efficiency is everything. Yet, for many establishments, especially in high-volume settings like Chinese restaurants, the point-of-sale (POS) system can be a bottleneck rooted in the past.

Waiters scribble orders on paper pads, then queue at a central terminal to input them, risking errors and delays. Managers struggle to get a real-time view of table occupancy. Takeaway counters get swamped, leading to long waits.

This is the problem **Microfood5** was born to solve. It's an open-source, **modern, tablet-based POS client** written in Kotlin for Android, designed to bring the power of the terminal directly to the point of service: the waiter's hand.

![Example Microfood5 Tablet Interface](https://www.kassaku.nl/wp-content/uploads/2025/10/Frida-app-1024x566.jpg)

### **The Problem: Paper, Delays, and Inefficiency**

Traditional POS workflows create friction at multiple points:

1.  **The Paper Trail:** Waiters use notepads, leading to illegible handwriting and error-prone transcription.
2.  **The Bottleneck:** Limited stationary terminals mean waiters waste time walking back and forth during peak hours.
3.  **The Visibility Gap:** Hosts can't instantly see which tables are free, clean, and ready.
4.  **The Takeaway Logjam:** A surge of to-go orders can overwhelm a single counter terminal, creating long lines.

### **The Solution: A POS in the Palm of Your Hand**

Microfood5 reimagines this workflow by turning an Android tablet into a powerful, mobile POS terminal. It functions as a remote client within a proven POS ecosystem, communicating seamlessly with a central server via **gRPC** for fast, reliable data exchange.

**Here's how it transforms the restaurant floor:**

*   **Order at the Table:** Waiters take orders directly on the tablet. The order is sent to the kitchen printer **immediately** as the waiter walks away.
*   **Real-Time Table Management:** A clear floor plan shows occupied and free tables at a glance. Hosts can seat guests faster.
*   **Mobilize Your Staff:** Any waiter with a tablet can process orders from anywhere. Managers can help input orders during rush hours.
*   **Accuracy and Transparency:** Customers see their order and running total on the spot, including **dine-in and takeaway pricing**.
*   **Multi-Language Ready:** The interface supports **Chinese (Traditional and Simplified), Indonesian, German, English, and Turkish**.

### **Proven Foundation, Modern Interface**

This isn't just a prototype. While this Kotlin client is in active development, it connects to a **battle-tested C++ POS server** already deployed in numerous restaurants across the Netherlands. This gives Microfood5 a unique advantage: modern mobile experience backed by years of real-world reliability.

**Architecture Philosophy:**
```
[Kotlin Tablet UI] <--gRPC--> [C++ POS Server Core] <---> [Hardware Peripherals]
```

The architecture is language-agnostic: business logic resides in the central server, while the user interface can be implemented in any technology that supports gRPC. This makes Microfood5 an excellent example for developers learning to build reactive Android layouts for complex workflows.

### **See It in Action**

February 2026
| Main Menu | Table Selection |Page Order|
|-----------|-----------------|----------|
|<img width="800" height="1340" alt="image" src="https://github.com/user-attachments/assets/6041a764-7c6b-47a4-8ffc-1abae703a7c0" />|<img width="800" height="1340" alt="image" src="https://github.com/user-attachments/assets/1bf48300-9dfb-4dd6-b4e6-6ce297e05fd5" />|<img width="800" height="1340" alt="image" src="https://github.com/user-attachments/assets/35e4768f-5572-48cf-8f3e-0f3f7498a5a5" />|

February 2026
| Landscape |
|-----------|
| <img width="1340" height="800" alt="image" src="https://github.com/user-attachments/assets/b06588a4-247d-43ae-aa02-fada65e335f0" /> |

February 2026
|Mobile phone   | Start | Page Order | Ask Transaction | Ask Table | Billing |
|---------------|-------|------------|-----------------|-----------|
| Samsug SM-A405FN | <img width="1080" height="2340" alt="image" src="https://github.com/user-attachments/assets/095d212f-676f-4251-b874-5ca7fa008315" /> | <img width="1080" height="2340" alt="image" src="https://github.com/user-attachments/assets/9de3dae7-692a-4beb-95dd-feb661be05a8" /> | <img width="1080" height="2340" alt="image" src="https://github.com/user-attachments/assets/b61e7525-bba1-4d34-93e7-17ad19a926c1" /> | <img width="1080" height="2340" alt="image" src="https://github.com/user-attachments/assets/e7fb8812-4c8b-4431-bb97-1c14371289c4" /> | <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/9a234c3e-fcc5-4356-9860-8352261e3a55" /> |








### **For Developers: Building on Solid Foundations**

Microfood5 serves as a robust reference implementation for:

*   Building modern, reactive Android layouts for POS workflows
*   Implementing full Point-of-Sale interfaces (order entry, modifications, payment)
*   Using gRPC as a high-performance communication layer between different programming languages

**Key Integration Point: The Proto Files**
The `.proto` files define the contract between client and server. They're the single source of truth for all communication. To build your own client in any language (JavaScript, Python, Go, etc.):

1. Use the provided `.proto` files to generate client code for your language
2. Implement the UI logic that calls the generated gRPC methods

This ensures seamless, type-safe interoperability with the POS server core.

### **Current Status & Roadmap**

*   **Status:** In Active Development (Kotlin client)
*   **Foundation:** C++ server software is mature and running reliably in real restaurants
*   **Future Goal:** When the Kotlin tablet app reaches maturity, it will be deployed alongside existing C++ systems, offering partner restaurants a sleek, modern table-side ordering experience

### **About Kassaku – Your POS Partner**

![Example working project in restaurant](https://www.kassaku.nl/wp-content/uploads/2023/07/wok.jpg)

Kassaku (Indonesian for *Kassa Aku* = "My Cash Register") is built on **over 15 years of experience** in the hospitality industry, founded by Bart Houkes.

**Our Story:** In 1994, Bart began his journey in the POS world at a company specializing in solutions for Asian restaurants. After a successful career as a software architect, a chance encounter in 2010 with a restaurateur led to the founding of Houkes Horeca Applications. For over 15 years, we have been the POS specialist for hospitality businesses that value simplicity and quality.

Our systems are built on core values: **Love, Quality, Accountability, Trust, and Recognition**. Whether you run a fast-service restaurant, a snack bar, or a cafeteria, our cash registers are designed to save time and reduce errors. No complicated manuals needed – your staff will work with it smoothly on the day of installation.

### **Get Involved**

Microfood5 represents a pragmatic open-source model:

*   **The Client (Android App)** is fully open-source (Apache 2.0 License). Restaurants or developers can inspect, modify, and build the client themselves.
*   **The Server** is offered as a licensed service, ensuring reliability, security, and ongoing updates.

**Join us in building the future of restaurant technology:**

*   **Restaurateurs:** Explore the client and inquire about server licensing for your business
*   **Android Developers:** Study the codebase, suggest features, or contribute improvements
*   **Translators:** Help expand its reach with more language support

**Ready to see the future of mobile POS?**

🔗 **Explore the Microfood5 repository on GitHub:** [https://github.com/barthoukes/microfood5](https://github.com/barthoukes/microfood5)

---

**Why I Built This:** Ultimately, Microfood5 bridges a personal passion for efficient technology with 15+ years of industry experience. It's about replacing clunky processes with sleek, mobile solutions that let restaurant staff focus on what they do best: providing great service.

*Interested in what our system can do for your business? Visit [www.kassaku.nl](https://www.kassaku.nl) to request a free, no-obligation demonstration!*
