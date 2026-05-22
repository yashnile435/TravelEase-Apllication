---
name: Modern Premium Travel
colors:
  surface: '#f8f9fb'
  surface-dim: '#d9dadc'
  surface-bright: '#f8f9fb'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f3f4f6'
  surface-container: '#edeef0'
  surface-container-high: '#e7e8ea'
  surface-container-highest: '#e1e2e4'
  on-surface: '#191c1e'
  on-surface-variant: '#434654'
  inverse-surface: '#2e3132'
  inverse-on-surface: '#f0f1f3'
  outline: '#737685'
  outline-variant: '#c3c6d6'
  surface-tint: '#0c56d0'
  primary: '#003d9b'
  on-primary: '#ffffff'
  primary-container: '#0052cc'
  on-primary-container: '#c4d2ff'
  inverse-primary: '#b2c5ff'
  secondary: '#825500'
  on-secondary: '#ffffff'
  secondary-container: '#feaa00'
  on-secondary-container: '#684300'
  tertiary: '#314368'
  on-tertiary: '#ffffff'
  tertiary-container: '#495a81'
  on-tertiary-container: '#c1d2ff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dae2ff'
  primary-fixed-dim: '#b2c5ff'
  on-primary-fixed: '#001848'
  on-primary-fixed-variant: '#0040a2'
  secondary-fixed: '#ffddb3'
  secondary-fixed-dim: '#ffb950'
  on-secondary-fixed: '#291800'
  on-secondary-fixed-variant: '#624000'
  tertiary-fixed: '#d8e2ff'
  tertiary-fixed-dim: '#b4c6f3'
  on-tertiary-fixed: '#051a3e'
  on-tertiary-fixed-variant: '#35466c'
  background: '#f8f9fb'
  on-background: '#191c1e'
  surface-variant: '#e1e2e4'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  headline-sm:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 8px
  container-padding-mobile: 16px
  container-padding-desktop: 24px
  gutter: 16px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 32px
  section-gap: 64px
---

## Brand & Style

The design system is anchored in the concept of "Modern Premium Utility." It aims to evoke a sense of professional reliability and effortless exploration. The target audience consists of discerning travelers who value clarity, speed, and a high-end aesthetic that doesn't sacrifice functional depth.

The visual style blends **Minimalism** with **Corporate Modern** sensibilities. By utilizing expansive whitespace, the UI reduces cognitive load, allowing high-quality destination and vehicle photography to serve as the primary emotional driver. The interface remains grounded through structured layouts and precise typography, ensuring the user feels in control of their journey at every touchpoint.

## Colors

This design system utilizes a palette designed for trust and immediate action. 

- **Primary (Travel Blue):** Used for core branding, active states, and navigation elements. It conveys stability and institutional knowledge.
- **Secondary (Action Orange):** Reserved exclusively for high-priority Calls to Action (CTAs) and critical conversion points. Its vibrance ensures it stands out against the cooler primary tones.
- **Neutral (Slate Grays & Whites):** A range of cool grays (Slate) provides the structural hierarchy. Surfaces are primarily crisp white to maintain an "airy" feel, while light grays define container boundaries.
- **Success/Warning/Error:** Standard semantic colors should be desaturated to match the professional tone of the primary blue.

## Typography

The design system relies on **Inter** for all typographic needs. The typeface was chosen for its exceptional legibility in technical contexts and its clean, modern geometric structure.

- **Headlines:** Use tighter letter-spacing and heavier weights (600-700) to create a strong visual anchor.
- **Body Text:** Maintained at a comfortable 16px base with generous line-height to support the "airy" brand pillar.
- **Labels:** Small labels and captions should utilize medium weights to remain legible against varied background grays.
- **Hierarchy:** Use color (Slate 700 vs Slate 900) rather than just size to distinguish between primary information and metadata.

## Layout & Spacing

This design system follows an **8px grid-based fluid layout**. Consistency in spacing reinforces the feeling of professional precision.

- **Desktop:** 12-column grid with a maximum content width of 1280px. 24px gutters.
- **Tablet:** 8-column grid with 24px margins.
- **Mobile:** 4-column grid with 16px margins.
- **Vertical Rhythm:** Elements within a component (e.g., card title to body) use `stack-sm` or `stack-md`. Major sections on a page use `section-gap` to preserve the airy aesthetic.

## Elevation & Depth

To maintain a premium feel, elevation is used sparingly and subtly. The design system avoids heavy, dark shadows in favor of **ambient light-diffused depth**.

- **Level 0 (Flat):** Used for the main canvas background (#FFFFFF).
- **Level 1 (Card):** Used for primary content containers. Features a very soft, multi-layered shadow: `0px 4px 12px rgba(0, 0, 0, 0.05)`.
- **Level 2 (Hover/Active):** Slightly more pronounced depth to indicate interactivity: `0px 8px 24px rgba(0, 0, 0, 0.08)`.
- **Level 3 (Overlays):** Used for modals and dropdowns. Includes a subtle 1px border in Slate 200 (#EBECF0) to ensure definition against light backgrounds.

Avoid using shadows on text or small icons; depth is strictly a container-level property.

## Shapes

The shape language is defined by **significant rounding** to evoke friendliness and modern comfort. 

- **Base Components:** Standard buttons and input fields use a 0.5rem (8px) radius.
- **Content Containers:** Large cards and image wrappers use `rounded-lg` (16px) to create a distinct, soft frame for travel photography.
- **Floating Elements:** Search bars and secondary tags may use `rounded-xl` (24px) or full pill-shapes to differentiate them from structural grid elements.
- **Iconography:** Use a consistent 2px stroke weight with rounded caps and joins to match the component corner radius.

## Components

### Buttons
- **Primary:** Travel Blue background with White text. Bold, sans-serif labels.
- **CTA:** Action Orange background with White text. Used only for "Book Now," "Reserve," or "Finalize."
- **Ghost:** Transparent background with Travel Blue border and text for secondary actions.

### Cards
- Travel cards must feature a 16:9 or 4:3 aspect ratio image at the top with `rounded-lg` corners. 
- Content padding should be at least 20px to maintain the spacious feel.
- Metadata (price, rating, duration) should use `label-sm` in a medium slate gray.

### Input Fields
- Enclosed boxes with a light gray fill (#F4F5F7) and no border in default state.
- On focus, transition to a Travel Blue 2px border with a soft blue outer glow.

### Chips & Tags
- Used for categories (e.g., "Luxury," "Off-road," "All-inclusive").
- Low-contrast backgrounds (Light Blue or Light Gray) with dark text. Full pill-shaped rounding.

### Imagery
- Photography is a core component. Always use high-resolution, high-brightness images with a natural, warm color grade. Avoid overly filtered or "stock" looking photos.