---
name: Professional Transit System
colors:
  surface: '#f9f9ff'
  surface-dim: '#d8d9e3'
  surface-bright: '#f9f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f3fd'
  surface-container: '#ecedf7'
  surface-container-high: '#e6e8f2'
  surface-container-highest: '#e0e2ec'
  on-surface: '#191c23'
  on-surface-variant: '#414754'
  inverse-surface: '#2d3038'
  inverse-on-surface: '#eff0fa'
  outline: '#727785'
  outline-variant: '#c1c6d6'
  surface-tint: '#005bc0'
  primary: '#005bbf'
  on-primary: '#ffffff'
  primary-container: '#1a73e8'
  on-primary-container: '#ffffff'
  inverse-primary: '#adc7ff'
  secondary: '#964900'
  on-secondary: '#ffffff'
  secondary-container: '#fc820c'
  on-secondary-container: '#5e2c00'
  tertiary: '#9e4300'
  on-tertiary: '#ffffff'
  tertiary-container: '#c55500'
  on-tertiary-container: '#0e0200'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d8e2ff'
  primary-fixed-dim: '#adc7ff'
  on-primary-fixed: '#001a41'
  on-primary-fixed-variant: '#004493'
  secondary-fixed: '#ffdcc6'
  secondary-fixed-dim: '#ffb786'
  on-secondary-fixed: '#311300'
  on-secondary-fixed-variant: '#723600'
  tertiary-fixed: '#ffdbcb'
  tertiary-fixed-dim: '#ffb691'
  on-tertiary-fixed: '#341100'
  on-tertiary-fixed-variant: '#783100'
  background: '#f9f9ff'
  on-background: '#191c23'
  surface-variant: '#e0e2ec'
typography:
  headline-lg:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 34px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.05em
  ticket-number:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '700'
    lineHeight: 20px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  container-margin: 1rem
  stack-space: 1.5rem
  inline-gutter: 1rem
  touch-target-min: 48px
---

## Brand & Style

The design system is engineered for a seamless, high-utility transit experience. It prioritizes clarity, speed of interaction, and a sense of reliability essential for travel planning. The brand personality is dependable and efficient, catering to daily commuters and long-distance travelers alike.

The aesthetic follows a **Corporate / Modern** style. It utilizes a flat design foundation enhanced by functional depth. By leveraging generous whitespace, the UI reduces cognitive load during the high-intent booking process. Visual hierarchy is established through purposeful color application rather than decorative elements, ensuring the interface feels tool-like yet welcoming.

## Colors

The palette is anchored by **Trustworthy Blue**, used for primary actions, navigation headers, and branding elements to instill confidence. **Energetic Orange** serves as a high-visibility accent color, reserved exclusively for conversion points, "Book Now" buttons, and critical alerts to draw immediate user attention.

Semantic colors are strictly defined for ticket lifecycles:
- **Success (Green):** Confirmed tickets and successful payments.
- **Pending (Yellow):** Reservations on hold or awaiting payment verification.
- **Cancelled (Red):** Voided trips or failed transactions.

The background uses a soft off-white to reduce glare, while text adheres to a high-contrast dark charcoal to ensure readability in various lighting conditions typical of travel environments.

## Typography

The design system utilizes **Inter** for its exceptional legibility and systematic appearance. The type scale is optimized for mobile glanceability, particularly for departure times and seat numbers.

- **Headlines:** Use Bold and Semibold weights to anchor sections like "Select Route" or "Trip Details."
- **Body:** The Medium weight is the workhorse for informational descriptions.
- **Labels:** Uppercase tracking is applied to labels for ticket IDs and status indicators to distinguish them from standard prose.

## Layout & Spacing

The layout employs a **Fluid Grid** system tailored for mobile viewports. A standard 4-column mobile grid is used with 16px (1rem) side margins. 

Spacing follows an 8pt rhythm to maintain mathematical harmony. Components like bus cards and seat selectors are separated by 24px (1.5rem) to ensure the interface feels "airy" and prevents accidental taps. Information density is kept moderate: high in the "Search Results" view to allow comparison, but low in the "Checkout" and "Digital Ticket" views to focus the user on single tasks.

## Elevation & Depth

This design system uses **Ambient Shadows** to create a subtle sense of layering without breaking the flat design aesthetic. Surfaces are tiered as follows:

1.  **Level 0 (Base):** The main background using the neutral base color.
2.  **Level 1 (Cards):** White surfaces with a very soft, diffused shadow (0px 4px 12px rgba(0,0,0,0.05)). This is the primary container for bus trip details.
3.  **Level 2 (Active Elements):** Primary buttons and active selection chips.
4.  **Level 3 (Sticky Nav):** Bottom navigation bars and floating action buttons utilize a slightly more pronounced shadow to indicate they sit above the scrolling content.

Overlays and modals should use a 40% opacity black backdrop blur to maintain context while focusing on the foreground task.

## Shapes

The design system utilizes a **Rounded** shape language with a base radius of 12px (0.75rem). This radius is applied consistently to all primary containers, input fields, and buttons to create a friendly, modern feel. 

- **Containers:** 12px for cards and modals.
- **Small Elements:** 8px for selection chips and status tags.
- **Interactive Elements:** Full-width buttons maintain the 12px corner radius to align with container edges.

## Components

### Buttons
- **Primary:** Filled Trustworthy Blue with white text.
- **Accent (CTA):** Filled Energetic Orange for "Book Now" or "Proceed to Pay."
- **Secondary:** Outlined blue with 1px border.

### Ticket Cards
The central component of the app. It should feature a white background, 12px rounded corners, and a subtle dashed divider to separate the "Route" section from the "Price/Status" section.

### Input Fields
Soft grey background (#F1F3F4) with 12px corners. On focus, the border transitions to 2px Solid Trustworthy Blue.

### Status Chips
Compact pill-shaped indicators. They use a light tinted background of the status color (e.g., 10% opacity Green) with high-contrast bold text of the same hue.

### Seat Selector
A custom grid component. "Available" seats are outlined, "Selected" seats are filled Primary Blue, and "Occupied" seats are flat grey.

### Progress Stepper
A slim horizontal bar at the top of the booking flow using the accent color to indicate the current stage (Search > Seats > Passenger > Payment).