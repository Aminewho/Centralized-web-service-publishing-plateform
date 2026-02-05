/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'rne-dark': '#1a2b49',
        'rne-cyan': '#3ab1bb',
      },
    },
  },
  plugins: [],
}