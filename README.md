# Flight Simulatior

A Java Swing-based flight simulation application that allows users to manage airports and flights in a visual, interactive environment.

## Features

- **Airport Management**
  - Add, edit, and remove airports
  - Each airport has a 3-letter code, name, and coordinates (X/Y)
  - Airports are displayed graphically on a map

- **Flight Management**
  - Schedule flights between airports
  - Specify takeoff time and duration
  - Flights are animated in real-time simulation

- **Import/Export**
  - Import and export airports and flights via CSV
  - Validation included for data integrity

- **Simulation**
  - Flights are visualized moving between airports
  - IdleManager handles application inactivity with warnings and automatic closure

- **GUI Features**
  - Responsive modal dialogs for creating and editing flights and airports
  - Error handling with user-friendly messages
  - Scalable map rendering
