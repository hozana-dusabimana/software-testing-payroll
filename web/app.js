// Shared shell + helpers for every page.
const ICONS = {
  home: '<path d="M3 11l9-8 9 8v9a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z"/>',
  users: '<circle cx="9" cy="8" r="3.5"/><path d="M2.5 20c0-3.5 3-6 6.5-6s6.5 2.5 6.5 6"/><path d="M16 4.5a3.5 3.5 0 0 1 0 7M18 14c2.3.7 3.5 2.6 3.5 5"/>',
  calc: '<rect x="5" y="3" width="14" height="18" rx="2"/><path d="M8 7h8M8 12h2M14 12h2M8 16h2M14 16h2"/>',
  file: '<path d="M6 3h8l4 4v14H6z"/><path d="M14 3v4h4M9 13h6M9 17h6"/>',
  check: '<path d="M4 12l5 5L20 6"/>',
  plus: '<path d="M12 5v14M5 12h14"/>',
  print: '<path d="M7 9V3h10v6M7 17H4v-6h16v6h-3M7 14h10v7H7z"/>',
};
const icon = n => `<svg viewBox="0 0 24 24">${ICONS[n]}</svg>`;

const NAV = [
  ["Main", [["index.html", "Dashboard", "home"], ["employees.html", "Employees", "users"], ["payroll.html", "Pay Calculator", "calc"], ["reports.html", "Payslips & Reports", "file"]]],
  ["Quality", [["qa.html", "QA Console", "check"]]],
];

function buildShell() {
  const here = location.pathname.split("/").pop() || "index.html";
  document.getElementById("side").innerHTML =
    `<div class="brand"><div class="brand-logo">${icon("users").replace("<svg", '<svg style="stroke:#fff;width:18px;height:18px"')}</div><div>PayDesk<small>Employee &amp; Payroll</small></div></div>` +
    NAV.map(([label, items]) => `<div class="nav-label">${label}</div>` +
      items.map(([href, text, ic]) => `<a class="nav ${href === here ? "active" : ""}" href="${href}">${icon(ic)}${text}</a>`).join("")).join("");
  const b = document.body.dataset;
  document.getElementById("top").innerHTML =
    `<div><h1>${b.title || ""}</h1><p>${b.sub || ""}</p></div><div class="user"><span>Signed in as HR Admin</span><div class="avatar">HA</div></div>`;
  const t = document.createElement("div"); t.id = "toasts"; document.body.appendChild(t);
}
document.addEventListener("DOMContentLoaded", buildShell);

// Calls the Java backend. Always resolves to {ok, message, ...}; never throws to the page.
async function api(method, url, data) {
  try {
    const res = await fetch(url, {
      method,
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: data ? new URLSearchParams(data).toString() : undefined,
    });
    return await res.json();
  } catch (e) {
    return { ok: false, message: "Cannot reach the server. Please make sure the application is running." };
  }
}

function toast(text, isErr) {
  const el = document.createElement("div");
  el.className = "toast" + (isErr ? " err" : "");
  el.textContent = text;
  document.getElementById("toasts").appendChild(el);
  setTimeout(() => el.remove(), 3500);
}

function esc(s) {
  return String(s ?? "").replace(/[&<>"']/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}
const initials = n => String(n || "?").split(/\s+/).slice(0, 2).map(w => w[0]).join("").toUpperCase();
const money = v => Number(v).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });

// Modal helper: returns the overlay; call .close() to remove it.
function openModal(html) {
  const o = document.createElement("div");
  o.className = "overlay";
  o.innerHTML = `<div class="modal">${html}</div>`;
  o.close = () => o.remove();
  o.addEventListener("mousedown", e => { if (e.target === o) o.close(); });
  document.body.appendChild(o);
  return o;
}
function confirmBox(title, text, okLabel) {
  return new Promise(resolve => {
    const m = openModal(`<h2>${esc(title)}</h2><p class="muted">${esc(text)}</p>
      <div class="actions"><button class="btn ghost" id="c-no">Cancel</button><button class="btn danger" id="c-yes">${esc(okLabel || "Confirm")}</button></div>`);
    m.querySelector("#c-no").onclick = () => { m.close(); resolve(false); };
    m.querySelector("#c-yes").onclick = () => { m.close(); resolve(true); };
  });
}
