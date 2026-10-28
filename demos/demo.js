// Recipedia — visual-only demo. Designed for a 390x844 phone screen.

const ROOT_CLASS = "demo-recipedia";
const FOOD_IMG = new URL("./recipedia/assets/alfredo.jpg", import.meta.url).href;

// ───────────────────────── Sample data ─────────────────────────
const RECIPES = [
  {
    id: 1, title: "Pasta Alfredo", mins: 30,
    desc: "Creamy, garlicky fettuccine tossed in a silky parmesan sauce. Ready in under half an hour.",
    tags: ["Dinner", "Pasta", "Vegetarian"],
    ings: [["8", "ounce", "Fettuccine", ""], ["1", "cup", "Heavy cream", ""], ["1", "cup", "Parmesan", "freshly grated"], ["2", "tablespoon", "Butter", ""], ["2", "clove", "Garlic", "minced"]],
    steps: [["Boil the pasta", "Cook the fettuccine in well salted water until al dente, then drain and save a splash of the pasta water."], ["Make the sauce", "Melt the butter, add the garlic, then pour in the cream and simmer gently until slightly thickened."], ["Toss and serve", "Stir in the parmesan, add the pasta and toss until everything is coated. Finish with plenty of pepper."]],
  },
  {
    id: 2, title: "Tomato Soup", mins: 45,
    desc: "A cozy roasted tomato soup that pairs perfectly with a grilled cheese sandwich.",
    tags: ["Lunch", "Soup", "Vegan", "Gluten Free"],
    ings: [["2", "pound", "Roma tomatoes", "halved"], ["1", "unit", "Yellow onion", "diced"], ["2", "cup", "Vegetable broth", ""], ["1", "teaspoon", "Salt", ""]],
    steps: [["Roast", "Roast the tomatoes and onion at 425°F until the edges begin to char."], ["Simmer", "Transfer to a pot with the broth and simmer for 10 minutes."], ["Blend", "Blend until smooth and season to taste."]],
  },
  {
    id: 3, title: "Berry Smoothie", mins: 5,
    desc: "A thick and fruity smoothie to start the morning.",
    tags: ["Breakfast", "Smoothie", "Vegan"],
    ings: [["1", "cup", "Mixed berries", "frozen"], ["1", "unit", "Banana", ""], ["1", "cup", "Oat milk", ""]],
    steps: [["Blend", "Add everything to a blender and blend until completely smooth."], ["Serve", "Pour into a tall glass and enjoy right away."]],
  },
  {
    id: 4, title: "Tofu Curry", mins: 40,
    desc: "Crispy tofu simmered in a fragrant coconut curry sauce. Serve over steamed rice.",
    tags: ["Dinner", "Curry", "Tofu", "Spicy", "Dairy Free"],
    ings: [["14", "ounce", "Extra firm tofu", "cubed"], ["1", "can", "Coconut milk", ""], ["2", "tablespoon", "Red curry paste", ""], ["1", "cup", "Spinach", ""]],
    steps: [["Crisp the tofu", "Pan fry the tofu until golden on all sides."], ["Build the sauce", "Stir the curry paste into the coconut milk and bring to a simmer."], ["Combine", "Add the tofu and spinach and cook until the spinach has wilted."]],
  },
  {
    id: 5, title: "Choc Chip Cookies", mins: 25,
    desc: "Chewy in the middle, crisp at the edges, loaded with chocolate chunks.",
    tags: ["Dessert", "Sweet", "Vegetarian"],
    ings: [["2", "cup", "Flour", ""], ["1", "cup", "Butter", "softened"], ["1", "cup", "Chocolate chips", ""], ["0.5", "cup", "Brown sugar", ""]],
    steps: [["Cream", "Beat the butter and sugar until pale and fluffy."], ["Mix", "Fold in the flour and chocolate chips."], ["Bake", "Scoop onto a tray and bake at 350°F for 11 minutes."]],
  },
  {
    id: 6, title: "Greek Salad", mins: 15,
    desc: "Crunchy cucumber, ripe tomato, olives and feta with a bright lemon dressing.",
    tags: ["Lunch", "Salad", "Gluten Free"],
    ings: [["1", "unit", "Cucumber", "sliced"], ["2", "unit", "Tomatoes", "chopped"], ["0.5", "cup", "Feta", "crumbled"], ["2", "tablespoon", "Olive oil", ""]],
    steps: [["Chop", "Chop the vegetables into bite sized pieces."], ["Dress", "Toss with olive oil, lemon juice and a pinch of salt, then top with feta."]],
  },
  {
    id: 7, title: "Avocado Toast", mins: 10,
    desc: "Smashed avocado on toasted sourdough with chili flakes.",
    tags: ["Breakfast", "Vegan", "Spicy"],
    ings: [["2", "slice", "Sourdough", ""], ["1", "unit", "Avocado", "ripe"], ["1", "pinch", "Chili flakes", ""]],
    steps: [["Toast", "Toast the bread until golden."], ["Smash", "Mash the avocado with salt and spread it on top. Sprinkle with chili flakes."]],
  },
  {
    id: 8, title: "Veggie Stir Fry", mins: 20,
    desc: "A quick weeknight stir fry with crisp vegetables and a savory garlic ginger sauce.",
    tags: ["Dinner", "Protein", "Savory", "Dairy Free"],
    ings: [["2", "cup", "Broccoli", "florets"], ["1", "unit", "Bell pepper", "sliced"], ["3", "tablespoon", "Soy sauce", ""], ["1", "tablespoon", "Ginger", "grated"]],
    steps: [["Prep", "Slice all of the vegetables and mix the sauce."], ["Stir fry", "Cook over high heat for 5 minutes, add the sauce and toss until glossy."]],
  },
  {
    id: 9, title: "Banana Pancakes", mins: 20,
    desc: "Fluffy pancakes sweetened with ripe banana. Top with maple syrup.",
    tags: ["Breakfast", "Sweet", "Vegetarian"],
    ings: [["1", "cup", "Flour", ""], ["2", "unit", "Bananas", "mashed"], ["1", "cup", "Milk", ""]],
    steps: [["Whisk", "Whisk the batter until just combined."], ["Cook", "Pour onto a hot griddle and flip when bubbles appear."]],
  },
];
const SAVED_IDS = [1, 4, 5, 2];
const CREATED_IDS = [3, 8];
const byId = (id) => RECIPES.find((r) => r.id === id);

const TAB_ORDER = ["home", "saved", "search", "discover", "settings"];

// ───────────────────────── Icons (Material, 24x24) ─────────────────────────
const P = {
  home: "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z",
  list: "M3 13h2v-2H3v2zm0 4h2v-2H3v2zm0-8h2V7H3v2zm4 4h14v-2H7v2zm0 4h14v-2H7v2zM7 7v2h14V7H7z",
  search: "M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z",
  explore: "M12 10.9c-.61 0-1.1.49-1.1 1.1s.49 1.1 1.1 1.1c.61 0 1.1-.49 1.1-1.1s-.49-1.1-1.1-1.1zM12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm2.19 12.19L6 18l3.81-8.19L18 6l-3.81 8.19z",
  settings: "M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z",
  add: "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z",
  clock: "M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z",
  account: "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z",
  edit: "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z",
  check: "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z",
  bookmark: "M17 3H7c-1.1 0-1.99.9-1.99 2L5 21l7-3 7 3V5c0-1.1-.9-2-2-2z",
  restaurant: "M11 9H9V2H7v7H5V2H3v7c0 2.12 1.66 3.84 3.75 3.97V22h2.5v-9.03C11.34 12.84 13 11.12 13 9V2h-2v7zm5-3v8h2.5v8H21V2c-2.76 0-5 2.24-5 4z",
  logout: "M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z",
  chevron: "M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z",
  upload: "M9 16h6v-6h4l-7-7-7 7h4zm-4 2h14v2H5z",
  photo: "M18 20H4V6h9V4H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2v-9h-2v9zm-7.79-3.17l-1.96-2.36L5.5 18h11l-3.54-4.71zM20 4V1h-2v3h-3c.01.01 0 2 0 2h3v2.99c.01.01 2 0 2 0V6h3V4h-3z",
  back: "M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z",
};
const icon = (name, size = 24, color = "currentColor") =>
  `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="${color}" aria-hidden="true"><path d="${P[name]}"/></svg>`;
// Outlined compass used by the Discover tab / placeholder
const compass = (size, color) =>
  `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="9"/><path d="M15.5 8.5l-2 5-5 2 2-5z"/></svg>`;
const TAB_ICON = { home: icon("home"), saved: icon("list"), search: icon("search"), discover: compass(24, "currentColor"), settings: icon("settings") };

// ───────────────────────── Styles ─────────────────────────
const CSS = `
.${ROOT_CLASS}, .${ROOT_CLASS} *, .${ROOT_CLASS} *::before, .${ROOT_CLASS} *::after { box-sizing: border-box; margin: 0; padding: 0; }
.${ROOT_CLASS} {
  --primary: #70CF97; --secondary: #3F7355; --tertiary: #B2E4C7; --gold: #FFD700;
  position: relative; width: 100%; height: 100%; overflow: hidden; background: #fff; color: #1C1B1F;
  font-family: -apple-system, "SF Pro Text", Roboto, "Segoe UI", system-ui, sans-serif;
  font-size: 16px; line-height: 24px; letter-spacing: .5px; user-select: none; -webkit-user-select: none;
  -webkit-tap-highlight-color: transparent;
}
.${ROOT_CLASS} .app { position: absolute; inset: 0; overflow: hidden; }
.${ROOT_CLASS} .stage { position: absolute; left: 0; right: 0; top: 0; bottom: 80px; overflow: hidden; }
.${ROOT_CLASS} .stage.full { bottom: 0; }
.${ROOT_CLASS} .screen { position: absolute; inset: 0; background: #fff; overflow-y: auto; overflow-x: hidden; scrollbar-width: none; transition: transform .25s ease, opacity .25s ease; will-change: transform, opacity; }
.${ROOT_CLASS} .screen::-webkit-scrollbar { display: none; }
.${ROOT_CLASS} .screen.fixed { overflow: hidden; display: flex; flex-direction: column; }
.${ROOT_CLASS} .scroll { flex: 1; min-height: 0; overflow-y: auto; scrollbar-width: none; }
.${ROOT_CLASS} .scroll::-webkit-scrollbar { display: none; }
.${ROOT_CLASS} .tappable { cursor: pointer; transition: opacity .1s, transform .1s; }
.${ROOT_CLASS} .tappable:active { opacity: .7; transform: scale(.97); }

/* status bar */
.${ROOT_CLASS} .status { position: absolute; top: 0; left: 0; right: 0; height: 50px; z-index: 40; pointer-events: none; display: flex; align-items: center; justify-content: space-between; padding: 6px 30px 0 34px; color: #fff; font-weight: 600; font-size: 15px; letter-spacing: 0; transition: color .25s; }
.${ROOT_CLASS} .status.dark { color: #1C1B1F; }
.${ROOT_CLASS} .status .r { display: flex; gap: 6px; align-items: center; }

/* bottom bar */
.${ROOT_CLASS} .bar { position: absolute; left: 0; right: 0; bottom: 0; height: 80px; background: #fff; z-index: 30; box-shadow: 0 -1px 0 rgba(0,0,0,.06); }
.${ROOT_CLASS} .tabs { display: flex; justify-content: space-evenly; height: 56px; }
.${ROOT_CLASS} .tab { display: flex; align-items: center; padding: 0 16px; height: 56px; color: rgba(0,0,0,.6); }
.${ROOT_CLASS} .tab.sel { color: #000; }
.${ROOT_CLASS} .tab span { display: none; margin-left: 12px; text-transform: uppercase; }
.${ROOT_CLASS} .tab.sel span { display: inline; }
.${ROOT_CLASS} .pill { position: absolute; bottom: 8px; left: 50%; width: 120px; height: 4px; margin-left: -60px; border-radius: 2px; background: rgba(0,0,0,.75); }
.${ROOT_CLASS} .backbtn { position: absolute; left: 14px; bottom: 6px; width: 40px; height: 36px; display: none; align-items: center; justify-content: center; color: rgba(0,0,0,.7); }
.${ROOT_CLASS} .backbtn.on { display: flex; }
.${ROOT_CLASS} .fab { position: absolute; left: 50%; margin-left: -28px; bottom: 96px; width: 56px; height: 56px; border-radius: 16px; background: var(--primary); color: #fff; display: flex; align-items: center; justify-content: center; z-index: 25; box-shadow: 0 4px 8px rgba(0,0,0,.28); opacity: 0; pointer-events: none; transition: opacity .2s, transform .1s; }
.${ROOT_CLASS} .fab.on { opacity: 1; pointer-events: auto; }
.${ROOT_CLASS} .fab.on:active { transform: scale(.94); }

/* Home */
.${ROOT_CLASS} .home-hero { background: var(--primary); padding: 50px 16px 25px; text-align: center; color: #fff; }
.${ROOT_CLASS} .home-title { font-size: 75px; line-height: 80px; font-weight: 700; letter-spacing: 0; text-shadow: 0 0 8px var(--secondary), 0 0 12px var(--secondary); }
.${ROOT_CLASS} .home-sub { margin-top: 15px; font-size: 22px; line-height: 28px; font-weight: 800; letter-spacing: 0; text-shadow: 0 0 8px var(--secondary), 0 0 12px var(--secondary); }
.${ROOT_CLASS} .home-btns { margin-top: 50px; display: flex; justify-content: space-evenly; }
.${ROOT_CLASS} .ebtn { background: var(--secondary); color: #fff; border-radius: 4px; padding: 0 24px; height: 56px; display: flex; align-items: center; font-size: 20px; letter-spacing: 0; box-shadow: 0 1px 3px rgba(0,0,0,.3); }
.${ROOT_CLASS} .home-body { padding: 16px; text-align: center; color: var(--secondary); }
.${ROOT_CLASS} .home-welcome { margin-top: 50px; text-align: center; color: var(--primary); }
.${ROOT_CLASS} .home-arrow { display: flex; justify-content: center; padding: 8px; color: var(--tertiary); }

/* search header (Saved + Search) */
.${ROOT_CLASS} .hdr { background: var(--primary); padding-top: 50px; flex: none; }
.${ROOT_CLASS} .field { margin: 8px; height: 56px; background: #fff; border: 1px solid #8a8a8a; border-radius: 15px; display: flex; align-items: center; padding: 0 16px; }
.${ROOT_CLASS} .field input { width: 100%; border: 0; outline: 0; background: transparent; font: inherit; color: #000; letter-spacing: .5px; user-select: text; -webkit-user-select: text; }
.${ROOT_CLASS} .field input::placeholder { color: #777; }
.${ROOT_CLASS} .field:focus-within { border: 2px solid var(--primary); padding: 0 15px; }
.${ROOT_CLASS} .toggle { display: inline-block; color: #fff; font-weight: 700; margin: 4px 0 8px 12px; letter-spacing: 0; }
.${ROOT_CLASS} .detail { overflow: hidden; max-height: 0; transition: max-height .25s ease; padding: 0 8px; }
.${ROOT_CLASS} .detail.open { max-height: 120px; }
.${ROOT_CLASS} .obtn { display: inline-flex; align-items: center; height: 40px; padding: 0 16px 0 12px; margin: 4px; border: 1px solid #fff; border-radius: 20px; color: #fff; letter-spacing: .1px; font-size: 14px; font-weight: 500; }
.${ROOT_CLASS} .obtn svg { margin-right: 6px; }
.${ROOT_CLASS} .detail .col { display: flex; flex-direction: column; align-items: flex-start; padding-bottom: 4px; }

/* recipe rows (Saved + Settings) */
.${ROOT_CLASS} .rows { padding: 8px 8px 24px; }
.${ROOT_CLASS} .row { display: flex; padding: 8px; border-radius: 12px; margin-bottom: 16px; box-shadow: 0 2px 8px rgba(0,0,0,.35); }
.${ROOT_CLASS} .row.g { background: var(--primary); color: #fff; --bg: var(--primary); --fg: #fff; }
.${ROOT_CLASS} .row.w { background: #fff; color: var(--primary); --bg: #fff; --fg: var(--primary); }
.${ROOT_CLASS} .thumb { width: 64px; height: 64px; flex: none; border-radius: 8px; background: currentColor; opacity: 1; position: relative; overflow: hidden; margin-right: 16px; }
.${ROOT_CLASS} .thumb::after { content: ""; position: absolute; inset: 0; background: var(--bg); opacity: .9; }
.${ROOT_CLASS} .rinfo { flex: 1; min-width: 0; }
.${ROOT_CLASS} .rtitle { font-size: 20px; line-height: 24px; font-weight: 700; }
.${ROOT_CLASS} .rtags { display: flex; margin-top: 2px; }
.${ROOT_CLASS} .rtag { font-size: 11px; line-height: 16px; font-weight: 500; letter-spacing: .5px; margin-right: 6px; padding: 2px 6px; border-radius: 4px; background: var(--fg); color: var(--bg); white-space: nowrap; }
.${ROOT_CLASS} .rdesc { font-size: 12px; line-height: 16px; letter-spacing: .4px; opacity: .85; margin-top: 2px; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }

/* Search grid */
.${ROOT_CLASS} .grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; padding: 12px; }
.${ROOT_CLASS} .card { position: relative; aspect-ratio: .9; border-radius: 12px; background: rgba(112,207,151,.85); overflow: hidden; }
.${ROOT_CLASS} .card .img { position: absolute; inset: 0; background: center / cover no-repeat; opacity: .5; }
.${ROOT_CLASS} .card .in { position: absolute; inset: 6px; display: flex; flex-direction: column; align-items: center; }
.${ROOT_CLASS} .card .top { flex: .75; text-align: center; width: 100%; }
.${ROOT_CLASS} .card .ct { color: #fff; font-size: 18px; line-height: 22px; font-weight: 800; letter-spacing: 0; text-shadow: 0 0 6px #000; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.${ROOT_CLASS} .card .tm { display: flex; justify-content: center; align-items: center; color: #fff; font-size: 14px; line-height: 20px; font-weight: 500; letter-spacing: .1px; text-shadow: 0 0 6px #000; }
.${ROOT_CLASS} .card .tm svg { margin-right: 4px; filter: drop-shadow(0 0 3px #000); }
.${ROOT_CLASS} .card .bot { flex: .25; display: flex; flex-wrap: wrap; gap: 6px; justify-content: center; align-content: center; width: 100%; }
.${ROOT_CLASS} .tp { height: 22px; padding: 0 4px; border-radius: 4px; background: var(--secondary); color: #fff; font-size: 10px; line-height: 22px; letter-spacing: 0; white-space: nowrap; }
.${ROOT_CLASS} .empty { display: none; text-align: center; color: var(--secondary); padding: 40px 16px; }
.${ROOT_CLASS} .empty.on { display: block; }

/* Recipe detail */
.${ROOT_CLASS} .rd-head { position: relative; background: var(--primary); color: #fff; padding: 50px 0 16px; flex: none; min-height: 40%; overflow: hidden; }
.${ROOT_CLASS} .rd-bg { position: absolute; inset: 0; background: center / cover no-repeat; opacity: .3; }
.${ROOT_CLASS} .rd-top { position: relative; display: flex; }
.${ROOT_CLASS} .rd-l { width: 60%; }
.${ROOT_CLASS} .rd-title { padding: 16px 16px 0; font-size: 38px; line-height: 40px; font-weight: 700; letter-spacing: 0; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.${ROOT_CLASS} .rd-cook { padding-left: 16px; margin-top: 4px; font-size: 15px; letter-spacing: 0; }
.${ROOT_CLASS} .rd-btn { margin: 8px 0 0 16px; width: 100px; height: 40px; border-radius: 4px; display: flex; align-items: center; justify-content: center; letter-spacing: 2px; background: #fff; color: var(--gold); box-shadow: 0 1px 3px rgba(0,0,0,.3); }
.${ROOT_CLASS} .rd-btn.saved { background: var(--gold); color: #fff; }
.${ROOT_CLASS} .rd-img { position: absolute; right: 8px; top: 24px; width: calc(40% - 8px); height: 128px; background: center / cover no-repeat; }
.${ROOT_CLASS} .rd-desc { position: relative; padding: 8px 16px 0; }
.${ROOT_CLASS} .rd-tags { position: relative; display: flex; flex-wrap: wrap; gap: 6px; padding: 12px 16px 0; }
.${ROOT_CLASS} .rd-tag { background: #fff; color: var(--primary); font-size: 14px; line-height: 16px; font-weight: 700; letter-spacing: 0; padding: 4px 8px; border-radius: 8px; }
.${ROOT_CLASS} .rd-ings { margin: 16px; border: 1px solid var(--secondary); border-radius: 8px; padding: 4px 0; }
.${ROOT_CLASS} .rd-ing { display: flex; padding: 4px 0; color: #000; }
.${ROOT_CLASS} .rd-ing .b { color: var(--secondary); padding: 0 8px; }
.${ROOT_CLASS} .rd-ing .n { font-weight: 700; margin: 0 4px 0 4px; }
.${ROOT_CLASS} .rd-ing .note { color: gray; font-style: italic; }
.${ROOT_CLASS} .rd-steps { padding: 16px 16px 40px; }
.${ROOT_CLASS} .rd-st { padding: 8px 8px 4px; font-size: 22px; line-height: 28px; font-weight: 800; letter-spacing: 0; }
.${ROOT_CLASS} .rd-sd { padding: 4px 8px 8px; }

/* Settings */
.${ROOT_CLASS} .st { padding: 50px 8px 12px; display: flex; flex-direction: column; height: 100%; }
.${ROOT_CLASS} .st-user { display: flex; flex-direction: column; align-items: center; color: var(--primary); }
.${ROOT_CLASS} .st-name { display: flex; align-items: center; color: #1C1B1F; font-size: 38px; line-height: 40px; font-weight: 700; letter-spacing: 0; margin-top: 4px; }
.${ROOT_CLASS} .st-name .e { margin-left: 8px; color: var(--primary); display: flex; }
.${ROOT_CLASS} .cards { display: flex; gap: 8px; margin: 14px 0 10px; }
.${ROOT_CLASS} .acard { flex: 1; border-radius: 12px; padding: 12px 6px; display: flex; flex-direction: column; align-items: center; background: #fff; color: var(--primary); box-shadow: 0 1px 4px rgba(0,0,0,.3); transition: background .2s; }
.${ROOT_CLASS} .acard.sel { background: var(--secondary); color: #fff; }
.${ROOT_CLASS} .acard .ic { width: 56px; height: 56px; border-radius: 50%; background: rgba(112,207,151,.15); display: flex; align-items: center; justify-content: center; }
.${ROOT_CLASS} .acard.sel .ic { background: rgba(255,255,255,.15); }
.${ROOT_CLASS} .acard .num { font-size: 22px; line-height: 28px; font-weight: 800; letter-spacing: 0; margin-top: 5px; }
.${ROOT_CLASS} .acard .lb { font-size: 14px; line-height: 16px; font-weight: 700; letter-spacing: 0; color: rgba(128,128,128,.7); text-align: center; }
.${ROOT_CLASS} .acard.sel .lb { color: #fff; }
.${ROOT_CLASS} .st-h { font-size: 22px; line-height: 28px; font-weight: 800; letter-spacing: 0; color: var(--primary); padding: 0 8px; }
.${ROOT_CLASS} .st-list { flex: 1; min-height: 0; overflow-y: auto; scrollbar-width: none; padding: 12px 0 0; }
.${ROOT_CLASS} .st-list::-webkit-scrollbar { display: none; }
.${ROOT_CLASS} .st-list .rows { padding: 0 8px; }
.${ROOT_CLASS} .signout { align-self: flex-start; margin: 4px 8px 4px; display: inline-flex; align-items: center; height: 40px; padding: 0 20px 0 12px; border: 1px solid red; border-radius: 20px; color: red; }
.${ROOT_CLASS} .signout svg { margin-right: 6px; }

/* Create */
.${ROOT_CLASS} .cr { padding: 50px 12px 28px; }
.${ROOT_CLASS} .cr-card { background: var(--primary); border-radius: 16px; padding: 16px; display: flex; flex-direction: column; gap: 20px; color: #fff; box-shadow: 0 2px 6px rgba(0,0,0,.3); }
.${ROOT_CLASS} .cr-obtn { display: inline-flex; align-items: center; height: 40px; padding: 0 16px 0 12px; border: 1px solid #fff; border-radius: 20px; font-size: 14px; font-weight: 500; letter-spacing: .1px; }
.${ROOT_CLASS} .cr-obtn svg { margin-right: 6px; }
.${ROOT_CLASS} .cr-h { font-size: 38px; line-height: 40px; font-weight: 700; letter-spacing: 0; }
.${ROOT_CLASS} .cr-s { font-size: 14px; line-height: 20px; letter-spacing: .25px; margin-top: 8px; }
.${ROOT_CLASS} .lab { font-size: 18px; line-height: 20px; font-weight: 700; letter-spacing: 0; margin-bottom: 8px; }
.${ROOT_CLASS} .inp { background: #fff; border-radius: 12px; min-height: 56px; display: flex; align-items: center; padding: 0 16px; color: var(--secondary); border: 1px solid transparent; }
.${ROOT_CLASS} .inp input, .${ROOT_CLASS} .inp textarea { width: 100%; border: 0; outline: 0; background: transparent; font: inherit; letter-spacing: .5px; color: var(--secondary); resize: none; user-select: text; -webkit-user-select: text; }
.${ROOT_CLASS} .inp.ta { align-items: flex-start; padding-top: 14px; min-height: 100px; }
.${ROOT_CLASS} .inp:focus-within { border-color: var(--secondary); }
.${ROOT_CLASS} .inp .suf { color: var(--secondary); margin-left: 4px; }
.${ROOT_CLASS} .imgbox { height: 160px; border-radius: 12px; background: rgba(255,255,255,.1); border: 1px solid rgba(255,255,255,.3); display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px; font-size: 14px; line-height: 20px; letter-spacing: .25px; }
.${ROOT_CLASS} .two { display: flex; gap: 16px; }
.${ROOT_CLASS} .two > div { flex: 1; min-width: 0; }
.${ROOT_CLASS} .chips { display: flex; flex-direction: column; gap: 4px; margin-bottom: 8px; }
.${ROOT_CLASS} .chip { background: #fff; color: var(--secondary); border-radius: 8px; padding: 4px 4px 4px 12px; display: flex; align-items: center; justify-content: space-between; font-size: 14px; line-height: 20px; letter-spacing: .25px; }
.${ROOT_CLASS} .chip svg { margin: 6px; }
.${ROOT_CLASS} .stepcard { background: #fff; color: #000; border-radius: 12px; padding: 0 8px 0 12px; display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.${ROOT_CLASS} .stepcard .t { font-size: 20px; line-height: 24px; font-weight: 700; padding-top: 8px; }
.${ROOT_CLASS} .stepcard .d { font-size: 14px; line-height: 20px; letter-spacing: .25px; padding: 2px 0 8px; }
.${ROOT_CLASS} .stepcard .ed { color: var(--secondary); padding: 8px; display: flex; }
.${ROOT_CLASS} .btns { display: flex; flex-direction: column; gap: 8px; }
.${ROOT_CLASS} .bfull { height: 40px; display: flex; align-items: center; justify-content: center; border-radius: 20px; border: 1px solid #fff; color: #fff; font-size: 14px; font-weight: 500; letter-spacing: .1px; }
.${ROOT_CLASS} .bfull.fill { height: 48px; border: 0; border-radius: 12px; background: var(--secondary); font-size: 18px; font-weight: 700; letter-spacing: 0; box-shadow: 0 1px 3px rgba(0,0,0,.3); }

/* Discover */
.${ROOT_CLASS} .soon { height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px; color: var(--primary); }
.${ROOT_CLASS} .soon .h { font-size: 22px; line-height: 28px; font-weight: 800; letter-spacing: 0; }
.${ROOT_CLASS} .soon .s { font-size: 14px; color: var(--secondary); letter-spacing: .25px; }
`;

// ───────────────────────── Helpers ─────────────────────────
const esc = (s) => String(s).replace(/&/g, "&amp;").replace(/</g, "&lt;");
const TAG_PRIORITY = ["Breakfast", "Lunch", "Dinner", "Snack", "Dessert", "Soup", "Salad", "Smoothie", "Pasta", "Curry", "Protein", "Tofu", "Spicy", "Sweet", "Savory", "Vegetarian", "Vegan", "Gluten Free", "Dairy Free"];
const sortTags = (tags) => [...tags].sort((a, b) => TAG_PRIORITY.indexOf(a) - TAG_PRIORITY.indexOf(b));
const fmtQty = (q) => ({ "0.5": "1/2", "0.25": "1/4", "1.5": "1 1/2" }[q] || q);

const statusBar = () => `
  <span>9:41</span>
  <span class="r">
    <svg width="18" height="12" viewBox="0 0 18 12" fill="currentColor"><rect x="0" y="8" width="3" height="4" rx=".8"/><rect x="5" y="5" width="3" height="7" rx=".8"/><rect x="10" y="2.5" width="3" height="9.5" rx=".8"/><rect x="15" y="0" width="3" height="12" rx=".8"/></svg>
    <svg width="17" height="12" viewBox="0 0 17 12" fill="currentColor"><path d="M8.5 2.3c2.3 0 4.4.9 6 2.4l1.1-1.2A10.5 10.5 0 0 0 8.5.6C5.8.6 3.3 1.600 1.400 3.500l1.100 1.200a8.800 8.800 0 0 1 6-2.400zm0 3.600c1.300 0 2.500.5 3.400 1.300l1.100-1.200a7.200 7.200 0 0 0-9 0l1.100 1.200c.9-.8 2.100-1.300 3.400-1.300zm0 3.400c-.6 0-1.200.2-1.600.6l1.600 1.800 1.600-1.800c-.4-.4-1-.6-1.600-.6z"/></svg>
    <svg width="27" height="13" viewBox="0 0 27 13" fill="none"><rect x=".5" y=".5" width="22" height="12" rx="3.500" stroke="currentColor" opacity=".45"/><rect x="2" y="2" width="19" height="9" rx="2" fill="currentColor"/><path d="M24 4.500v4c.8-.3 1.500-1.100 1.500-2s-.7-1.700-1.500-2z" fill="currentColor" opacity=".5"/></svg>
  </span>`;

// ───────────────────────── Screens ─────────────────────────
function rowHTML(r, i) {
  const tags = sortTags(r.tags).slice(0, 3).map((t) => `<span class="rtag">${t}</span>`).join("");
  return `<div class="row tappable ${i % 2 === 0 ? "g" : "w"}" data-act="recipe" data-id="${r.id}">
    <div class="thumb"></div>
    <div class="rinfo"><div class="rtitle">${esc(r.title)}</div><div class="rtags">${tags}</div><div class="rdesc">${esc(r.desc)}</div></div>
  </div>`;
}

function searchHeader(withDetail) {
  return `<div class="hdr">
    <label class="field"><input type="text" placeholder="Search recipes" data-filter="${withDetail}" autocomplete="off" spellcheck="false"></label>
    <div class="toggle tappable" data-act="detail">Show Detailed Search ▼</div>
    <div class="detail"><div class="col">
      <div class="obtn tappable">${icon("add", 18)}Filter by ingredient</div>
      <div class="obtn tappable">${icon("add", 18)}Filter by tag</div>
    </div></div>
  </div>`;
}

function homeScreen() {
  return `<div class="screen" data-status="light">
    <div class="home-hero">
      <div class="home-title">Recipedia</div>
      <div class="home-sub">Cook. Bake. Create.</div>
      <div class="home-btns">
        <div class="ebtn tappable" data-act="tab" data-tab="saved">Saved Recipes</div>
        <div class="ebtn tappable" data-act="tab" data-tab="discover">Find Recipes</div>
      </div>
    </div>
    <div class="home-body">Discover a world of culinary delights with Recipedia, your ultimate recipe companion. Discover, save, create recipes of all kinds. Find your next meal with what's around the house, or recreate your own masterpiece with our easy-to-use recipe storage. Whether you're a seasoned chef or a beginner in the kitchen, Recipedia has something for every user.<br><br>Choose an option above to begin your culinary journey, or swipe up to quickly navigate to your saved recipes</div>
    <div class="home-welcome" style="margin-top:50px">Welcome to Recipedia</div>
    <div class="home-arrow">${icon("chevron")}</div>
  </div>`;
}

function savedScreen() {
  const rows = SAVED_IDS.map((id, i) => rowHTML(byId(id), i)).join("");
  return `<div class="screen fixed" data-status="light">
    ${searchHeader("rows")}
    <div class="scroll"><div class="rows" data-list="rows">${rows}</div><div class="empty">No recipes found</div></div>
  </div>`;
}

function searchScreen() {
  const cards = RECIPES.map((r) => {
    const tags = sortTags(r.tags).slice(0, 2).map((t) => `<span class="tp">${t}</span>`).join("");
    return `<div class="card tappable" data-act="recipe" data-id="${r.id}" data-q="${esc((r.title + " " + r.desc).toLowerCase())}">
      <div class="img" style="background-image:url('${FOOD_IMG}')"></div>
      <div class="in"><div class="top"><div class="ct">${esc(r.title)}</div><div class="tm">${icon("clock", 16, "#fff")}${r.mins} min</div></div><div class="bot">${tags}</div></div>
    </div>`;
  }).join("");
  return `<div class="screen fixed" data-status="light">
    ${searchHeader("grid")}
    <div class="scroll"><div class="grid" data-list="grid">${cards}</div><div class="empty">No recipes found</div></div>
  </div>`;
}

function discoverScreen() {
  return `<div class="screen" data-status="dark"><div class="soon">
    ${compass(88, "#70CF97")}<div class="h">Coming soon</div><div class="s">Discover is on its way. Check back later!</div>
  </div></div>`;
}

function recipeScreen(id) {
  const r = byId(id);
  const saved = SAVED_IDS.includes(id);
  const tags = sortTags(r.tags).map((t) => `<span class="rd-tag">${t}</span>`).join("");
  const ings = r.ings.map(([q, u, n, note]) =>
    `<div class="rd-ing"><span class="b">•</span><span>${fmtQty(q)} ${u}&nbsp;</span><span class="n">${esc(n)}</span><span class="note">${esc(note)}</span></div>`).join("");
  const steps = r.steps.map(([t, d], i) =>
    `<div><div class="rd-st">Step ${i + 1}: ${esc(t)}</div><div class="rd-sd">${esc(d)}</div></div>`).join("");
  return `<div class="screen fixed" data-status="light">
    <div class="rd-head">
      <div class="rd-bg" style="background-image:url('${FOOD_IMG}')"></div>
      <div class="rd-top">
        <div class="rd-l">
          <div class="rd-title">${esc(r.title)}</div>
          <div class="rd-cook">Cook Time: ${r.mins} minutes</div>
          <div class="rd-btn tappable ${saved ? "saved" : ""}" data-act="save">${saved ? "Saved" : "Save"}</div>
        </div>
        <div class="rd-img" style="background-image:url('${FOOD_IMG}')"></div>
      </div>
      <div class="rd-desc">${esc(r.desc)}</div>
      <div class="rd-tags">${tags}</div>
    </div>
    <div class="scroll"><div class="rd-ings">${ings}</div><div class="rd-steps">${steps}</div></div>
  </div>`;
}

function settingsScreen(sel = 1) {
  const cards = [
    [1, "Recipes Created", String(CREATED_IDS.length), "edit"],
    [2, "Recipes Saved", String(SAVED_IDS.length), "bookmark"],
    [3, "Recipes Made", "0", "restaurant"],
  ].map(([n, label, num, ic]) => `<div class="acard tappable ${n === sel ? "sel" : ""}" data-act="card" data-n="${n}">
      <div class="ic">${icon(ic, 32)}</div><div class="num">${num}</div><div class="lb">${label}</div></div>`).join("");
  return `<div class="screen" data-status="dark"><div class="st">
    <div class="st-user">${icon("account", 100)}<div class="st-name">Adam<span class="e">${icon("edit", 30)}</span></div></div>
    <div class="cards">${cards}</div>
    <div class="st-h" data-role="heading">Recipes You Created</div>
    <div class="st-list" data-role="list">${settingsList(1)}</div>
    <div class="signout tappable">${icon("logout", 26)}<span>Sign Out</span></div>
  </div></div>`;
}
function settingsList(n) {
  if (n === 3) return `<div style="padding:0 8px">Coming soon...</div>`;
  const ids = n === 1 ? CREATED_IDS : SAVED_IDS;
  return `<div class="rows">${ids.map((id, i) => rowHTML(byId(id), i)).join("")}</div>`;
}

function createScreen() {
  const ing = (t) => `<div class="chip"><span>${t}</span>${icon("add", 16, "#3F7355").replace("<path", '<path transform="rotate(45 12 12)"')}</div>`;
  const outBtn = (t) => `<div class="cr-obtn tappable">${icon("add", 18)}${t}</div>`;
  return `<div class="screen" data-status="dark"><div class="cr"><div class="cr-card">
    <div><div class="cr-obtn tappable">${icon("upload", 18)}Import</div></div>
    <div><div class="cr-h">Create a Recipe</div><div class="cr-s">Fill in the details below to create your recipe.</div></div>
    <div><div class="lab">Recipe Title*</div><label class="inp"><input type="text" autocomplete="off"></label></div>
    <div><div class="lab">Recipe Description*</div><label class="inp ta"><textarea rows="3"></textarea></label></div>
    <div><div class="lab">Recipe Image</div><div class="imgbox tappable">${icon("photo", 48, "#fff")}<span>Tap to add image</span></div></div>
    <div class="two">
      <div><div class="lab">Prep Time*</div><label class="inp"><input type="text" inputmode="numeric"><span class="suf">min</span></label></div>
      <div><div class="lab">Recipe Style</div><label class="inp"><input type="text"></label></div>
    </div>
    <div><div class="lab">Recipe Ingredients*</div><div class="chips">${ing("2cup Flour")}${ing("1tsp Salt")}</div>${outBtn("Add ingredient")}</div>
    <div><div class="lab">Recipe Steps*</div>
      <div class="stepcard"><div><div class="t">1. Mix the dough</div><div class="d">Combine the flour and salt with warm water.</div></div><div class="ed">${icon("edit", 18, "#3F7355")}</div></div>
      ${outBtn("Add step")}</div>
    <div><div class="lab">Recipe Tags</div><div class="chips">${ing("Dinner")}</div>${outBtn("Add tag")}</div>
    <div class="btns"><div class="bfull tappable">Preview Recipe</div><div class="bfull fill tappable">Create Recipe</div></div>
  </div></div></div>`;
}

const SCREENS = {
  home: homeScreen, saved: savedScreen, search: searchScreen, discover: discoverScreen,
  settings: settingsScreen, create: createScreen, recipe: (p) => recipeScreen(p),
};

// ───────────────────────── Mount + navigation ─────────────────────────
export function mount(root) {
  root.classList.add(ROOT_CLASS);
  const style = document.createElement("style");
  style.textContent = CSS;

  const app = document.createElement("div");
  app.className = "app";
  app.innerHTML = `
    <div class="stage"></div>
    <div class="status">${statusBar()}</div>
    <div class="fab tappable" data-act="create">${icon("add", 24)}</div>
    <div class="bar">
      <div class="tabs">${TAB_ORDER.map((t) => `<div class="tab tappable" data-act="tab" data-tab="${t}">${TAB_ICON[t]}<span>${t}</span></div>`).join("")}</div>
      <div class="backbtn tappable" data-act="back">${icon("back", 28)}</div>
      <div class="pill"></div>
    </div>`;
  root.replaceChildren(style, app);

  const stage = app.querySelector(".stage");
  const status = app.querySelector(".status");
  const fab = app.querySelector(".fab");
  const backBtn = app.querySelector(".backbtn");
  const tabEls = [...app.querySelectorAll(".tab")];
  const timers = new Set();
  const later = (fn, ms) => { const t = setTimeout(() => { timers.delete(t); fn(); }, ms); timers.add(t); };

  let stack = [{ name: "home" }];
  let current = null;

  function build(entry) {
    const tpl = document.createElement("div");
    tpl.innerHTML = SCREENS[entry.name](entry.id).trim();
    return tpl.firstElementChild;
  }

  function chrome(entry) {
    const el = current;
    status.classList.toggle("dark", el.dataset.status === "dark");
    tabEls.forEach((t) => t.classList.toggle("sel", t.dataset.tab === entry.name));
    // FAB is hidden on Home, Settings and Create (as in the app)
    fab.classList.toggle("on", !["home", "settings", "create"].includes(entry.name));
    backBtn.classList.toggle("on", stack.length > 1);
  }

  // kind: "fade" (tab switch), "push" (slide in), "pop" (slide out), "none"
  function show(kind) {
    const entry = stack[stack.length - 1];
    const next = build(entry);
    const prev = current;
    current = next;
    chrome(entry);
    if (!prev || kind === "none") { stage.replaceChildren(next); return; }
    if (kind === "pop") {
      stage.insertBefore(next, prev);
      prev.style.zIndex = 2;
      void prev.offsetWidth; // force reflow so the transition runs
      prev.style.transform = "translateX(100%)";
    } else {
      next.style.zIndex = 2;
      next.style.transform = kind === "push" ? "translateX(100%)" : "none";
      next.style.opacity = kind === "fade" ? "0" : "1";
      stage.appendChild(next);
      void next.offsetWidth; // force reflow so the transition runs
      next.style.transform = "none";
      next.style.opacity = "1";
    }
    later(() => { [...stage.children].forEach((c) => { if (c !== next) c.remove(); }); next.style.zIndex = ""; }, 270);
  }

  function filterList(input) {
    const list = current.querySelector("[data-list]");
    const q = input.value.trim().toLowerCase();
    let shown = 0;
    [...list.children].forEach((c) => {
      const hay = (c.dataset.q || c.textContent).toLowerCase();
      const ok = !q || hay.includes(q);
      c.style.display = ok ? "" : "none";
      if (ok) shown++;
    });
    current.querySelector(".empty").classList.toggle("on", shown === 0);
  }

  // One delegated handler for all taps, attached to an element inside root.
  app.addEventListener("click", (e) => {
    const el = e.target.closest("[data-act]");
    if (!el || !app.contains(el)) return;
    const act = el.dataset.act;
    if (act === "tab") {
      const same = stack.length === 1 && stack[0].name === el.dataset.tab;
      if (same) return;
      stack = [{ name: el.dataset.tab }];
      show("fade");
    } else if (act === "recipe") {
      stack.push({ name: "recipe", id: Number(el.dataset.id) });
      show("push");
    } else if (act === "create") {
      stack.push({ name: "create" });
      show("push");
    } else if (act === "back") {
      if (stack.length > 1) { stack.pop(); show("pop"); }
    } else if (act === "detail") {
      const d = current.querySelector(".detail");
      const open = d.classList.toggle("open");
      el.textContent = open ? "Hide Detailed Search ▲" : "Show Detailed Search ▼";
    } else if (act === "save") {
      const on = el.classList.toggle("saved");
      el.textContent = on ? "Saved" : "Save";
    } else if (act === "card") {
      const n = Number(el.dataset.n);
      current.querySelectorAll(".acard").forEach((c) => c.classList.toggle("sel", c === el));
      current.querySelector("[data-role=heading]").textContent = ["", "Recipes You Created", "Recipes You Saved", "Coming soon.."][n];
      current.querySelector("[data-role=list]").innerHTML = settingsList(n);
    }
  });
  app.addEventListener("input", (e) => {
    if (e.target.matches("input[data-filter]")) filterList(e.target);
  });

  show("none");

  return () => {
    timers.forEach(clearTimeout);
    timers.clear();
    root.classList.remove(ROOT_CLASS);
    root.replaceChildren();
  };
}
