/** @type {import('tailwindcss').Config} */

export default {
  darkMode: "class",
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    container: {
      center: true,
      padding: { DEFAULT: "1.5rem", lg: "2rem" },
    },
    extend: {
      colors: {
        void: {
          DEFAULT: "#05060A",
          50: "#0A0B0F",
          100: "#10121A",
          200: "#161924",
          300: "#1E2230",
        },
        lumin: {
          DEFAULT: "#5EEAD4",
          dim: "#2DD4BF",
          glow: "rgba(94, 234, 212, 0.5)",
        },
        spectral: {
          DEFAULT: "#8B7CFF",
          dim: "#7C6FFF",
          glow: "rgba(139, 124, 255, 0.5)",
        },
        ink: {
          DEFAULT: "#E8EAF0",
          muted: "#8B8FA3",
          faint: "#5A5E72",
        },
      },
      fontFamily: {
        display: ['"Instrument Serif"', "serif"],
        sans: ['"Manrope"', "system-ui", "sans-serif"],
        mono: ['"JetBrains Mono"', "ui-monospace", "monospace"],
      },
      letterSpacing: {
        tightest: "-0.04em",
      },
      animation: {
        "fade-up": "fadeUp 0.9s cubic-bezier(0.22, 1, 0.36, 1) forwards",
        "fade-in": "fadeIn 1.2s ease forwards",
        shimmer: "shimmer 3s linear infinite",
        float: "float 6s ease-in-out infinite",
      },
      keyframes: {
        fadeUp: {
          "0%": { opacity: "0", transform: "translateY(24px)" },
          "100%": { opacity: "1", transform: "translateY(0)" },
        },
        fadeIn: {
          "0%": { opacity: "0" },
          "100%": { opacity: "1" },
        },
        shimmer: {
          "0%": { backgroundPosition: "-200% 0" },
          "100%": { backgroundPosition: "200% 0" },
        },
        float: {
          "0%, 100%": { transform: "translateY(0)" },
          "50%": { transform: "translateY(-8px)" },
        },
      },
    },
  },
  plugins: [],
};
