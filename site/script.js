/* Socket Client — интерактив лендинга */
(() => {
  "use strict";

  const $ = (s, r = document) => r.querySelector(s);
  const $$ = (s, r = document) => [...r.querySelectorAll(s)];
  const reduced = matchMedia("(prefers-reduced-motion: reduce)").matches;

  /* ---------- навигация: фон при скролле + активная ссылка ---------- */
  const nav = $("#nav");
  const links = $$("#navLinks a");

  const onScroll = () => {
    nav.classList.toggle("stuck", scrollY > 24);
  };
  onScroll();
  addEventListener("scroll", onScroll, { passive: true });

  const sections = links
    .map((a) => $(a.getAttribute("href")))
    .filter(Boolean);

  if (sections.length) {
    const spy = new IntersectionObserver(
      (entries) => {
        entries.forEach((e) => {
          if (!e.isIntersecting) return;
          links.forEach((a) =>
            a.classList.toggle("on", a.getAttribute("href") === "#" + e.target.id)
          );
        });
      },
      { rootMargin: "-45% 0px -50% 0px" }
    );
    sections.forEach((s) => spy.observe(s));
  }

  /* ---------- бургер ---------- */
  const burger = $("#burger");
  const navLinks = $("#navLinks");

  const closeMenu = () => {
    navLinks.classList.remove("open");
    burger.classList.remove("x");
    burger.setAttribute("aria-expanded", "false");
  };

  burger.addEventListener("click", () => {
    const open = navLinks.classList.toggle("open");
    burger.classList.toggle("x", open);
    burger.setAttribute("aria-expanded", String(open));
  });
  navLinks.addEventListener("click", (e) => {
    if (e.target.tagName === "A") closeMenu();
  });
  addEventListener("keydown", (e) => e.key === "Escape" && closeMenu());

  /* ---------- курсорное свечение ---------- */
  const glow = $("#glow");
  if (glow && !reduced && matchMedia("(pointer: fine)").matches) {
    let x = innerWidth / 2,
      y = innerHeight / 2,
      cx = x,
      cy = y,
      raf = null;

    addEventListener("pointermove", (e) => {
      x = e.clientX;
      y = e.clientY;
      glow.style.opacity = "1";
      if (!raf) raf = requestAnimationFrame(tick);
    });
    addEventListener("pointerleave", () => (glow.style.opacity = "0"));

    function tick() {
      cx += (x - cx) * 0.09;
      cy += (y - cy) * 0.09;
      glow.style.left = cx + "px";
      glow.style.top = cy + "px";
      raf =
        Math.abs(x - cx) + Math.abs(y - cy) > 0.5
          ? requestAnimationFrame(tick)
          : null;
    }
  }

  /* ---------- появление блоков при скролле ---------- */
  const rv = $$(".rv");
  if (reduced) {
    rv.forEach((el) => el.classList.add("in"));
  } else {
    const io = new IntersectionObserver(
      (entries, obs) => {
        entries.forEach((e) => {
          if (!e.isIntersecting) return;
          e.target.classList.add("in");
          obs.unobserve(e.target);
        });
      },
      { threshold: 0.14, rootMargin: "0px 0px -8% 0px" }
    );
    rv.forEach((el) => io.observe(el));
  }

  /* ---------- график FPS ---------- */
  const chart = $("#fpsChart");
  if (chart) {
    const bars = [34, 52, 45, 68, 60, 79, 72, 88, 82, 96, 91, 100];
    chart.innerHTML = bars
      .map(
        (h, i) =>
          `<i style="--h:${h}%;animation-delay:${(i * 0.06).toFixed(2)}s"></i>`
      )
      .join("");
  }

  /* ---------- счётчики ---------- */
  $$("[data-count]").forEach((el) => {
    const target = +el.dataset.count;
    if (reduced) {
      el.textContent = target;
      return;
    }
    const io = new IntersectionObserver(
      (entries, obs) => {
        if (!entries[0].isIntersecting) return;
        obs.disconnect();
        const dur = 1400,
          t0 = performance.now();
        const step = (now) => {
          const p = Math.min(1, (now - t0) / dur);
          el.textContent = Math.round(target * (1 - Math.pow(1 - p, 3)));
          if (p < 1) requestAnimationFrame(step);
        };
        requestAnimationFrame(step);
      },
      { threshold: 0.5 }
    );
    io.observe(el);
  });

  /* ---------- ClickGUI в hero ---------- */
  const guiMods = $("#guiMods");
  if (guiMods) {
    guiMods.addEventListener("click", (e) => {
      const mod = e.target.closest(".mod");
      if (mod) mod.classList.toggle("act");
    });
  }

  const cats = $$(".gui-cats button");
  cats.forEach((b) =>
    b.addEventListener("click", () => {
      cats.forEach((o) => o.classList.remove("on"));
      b.classList.add("on");
    })
  );

  const fps = $(".gui-bar .fps");
  if (fps && !reduced) {
    setInterval(() => {
      fps.textContent = 300 + Math.floor(Math.random() * 26) + " FPS";
    }, 1600);
  }

  /* ---------- модули: категории ---------- */
  const CATEGORIES = [
    {
      id: "visual",
      name: "Визуал",
      mods: [
        ["Adornments", "Крылья, ореол и орбита предметов на модели игрока.", "—"],
        ["Cape", "Плащ с физикой: наклон от скорости, волна ветра, качание после остановки.", "—"],
        ["China Hat", "Китайская шляпа на голове персонажа.", "—"],
        ["Enchant Glow", "Свой цвет свечения зачарованных предметов.", "—"],
        ["Item Physic", "Предметы на земле падают и крутятся физично.", "—"],
        ["Full Bright", "Полностью освещает мир через гамму или ночное зрение.", "—"],
        ["Removals", "Убирает выбранные визуальные эффекты и элементы игры.", "—"],
      ],
    },
    {
      id: "camera",
      name: "Камера",
      mods: [
        ["Zoom", "Плавное приближение по клавише с настройкой кратности.", "C"],
        ["Freelook", "Осмотр камерой без поворота игрока.", "V"],
        ["Motion Blur", "Смаз кадра при движении камеры за счёт накопления предыдущих кадров.", "—"],
        ["Aspect Ratio", "Изменяет соотношение сторон экрана.", "—"],
        ["View Model", "Изменяет положение и размер предметов в руке.", "—"],
        ["Swing Animation", "Настраивает анимацию взмаха руки.", "—"],
        ["Animations", "Анимирует выбранные элементы игры.", "—"],
      ],
    },
    {
      id: "hud",
      name: "Интерфейс",
      mods: [
        ["Interface", "Виджеты на экране: инфо-панель, броня, зелья, задержки, клавиши, таргет-худ, инвентарь, уведомления.", "—"],
        ["Item Info", "Питательность еды и прочность предмета в подсказке.", "—"],
        ["Item Highlight", "Подсвечивает нужные предметы в контейнерах.", "—"],
        ["Death Coords", "Координаты последней смерти и метка на месте.", "—"],
        ["Radial Menu", "Круговое меню быстрого доступа к модулям.", "—"],
        ["Chat Helper", "Расширяет возможности чата и его настройки.", "—"],
        ["Streamer Mode", "Скрывает личные данные при стриминге и записи.", "—"],
      ],
    },
    {
      id: "misc",
      name: "Разное",
      mods: [
        ["Inventory Sort", "Сортирует контейнер или инвентарь по клавише.", "R"],
        ["Item Scroller", "Перенос предметов колесом мыши в контейнерах.", "—"],
        ["Elytra Swap", "Меняет нагрудник и элитру по клавише.", "G"],
        ["Auto Swap", "Мгновенно перекладывает сферу или тотем во вторую руку.", "—"],
        ["Optimization", "Поднимает FPS: убирает дорогие эффекты и ограничивает дальность прорисовки.", "—"],
        ["Sprint", "Автоматически включает спринт при движении.", "—"],
        ["Sounds", "Воспроизводит выбранные звуки при игровых событиях.", "—"],
        ["Sound Reducer", "Уменьшает громкость выбранных игровых звуков.", "—"],
      ],
    },
  ];

  const tabs = $("#tabs");
  const grid = $("#modsGrid");

  if (tabs && grid) {
    tabs.innerHTML = CATEGORIES.map(
      (c, i) =>
        `<button role="tab" aria-selected="${i === 0}" data-cat="${c.id}" class="${
          i === 0 ? "on" : ""
        }"><i></i>${c.name}<small>${c.mods.length}</small></button>`
    ).join("");

    const render = (id) => {
      const cat = CATEGORIES.find((c) => c.id === id);
      grid.innerHTML = cat.mods
        .map(
          ([name, desc, key], i) => `
        <article class="mcard" style="animation-delay:${(i * 0.05).toFixed(2)}s">
          <div class="mcard-top"><b>${name}</b><span class="sw"></span></div>
          <p>${desc}</p>
          <span class="k">${key === "—" ? "без клавиши" : "клавиша " + key}</span>
        </article>`
        )
        .join("");
    };

    render(CATEGORIES[0].id);

    tabs.addEventListener("click", (e) => {
      const btn = e.target.closest("button");
      if (!btn) return;
      $$("button", tabs).forEach((b) => {
        const on = b === btn;
        b.classList.toggle("on", on);
        b.setAttribute("aria-selected", String(on));
      });
      render(btn.dataset.cat);
    });

    grid.addEventListener("click", (e) => {
      const card = e.target.closest(".mcard");
      if (card) card.classList.toggle("act");
    });
  }

  /* ---------- FAQ ---------- */
  const FAQ = [
    [
      "Это чит? За него забанят?",
      "Socket — визуальный клиент: он меняет то, как игра выглядит у вас на экране. Боевых функций в сборке нет. Но правила пишет администрация сервера, поэтому перед игрой на анархии или мини-играх стоит уточнить, что там разрешено.",
    ],
    [
      "Какая версия поддерживается?",
      "Fabric 1.21.4. Socket — не мод, который нужно докидывать в папку: у клиента свой лоадер, он сам поднимает нужное окружение и отдельный профиль, а ваша обычная сборка остаётся нетронутой.",
    ],
    [
      "Пойдёт на слабом ПК?",
      "Да. FPS остаётся на высоком уровне даже с включённым визуалом — клиент не тянет кадры вниз, как это обычно бывает. А тяжёлые эффекты вроде Motion Blur выключаются по одному, так что запас производительности всегда можно вернуть.",
    ],
    [
      "Клиент бесплатный?",
      "Да, полностью. Ни подписки, ни платных модулей, ни рекламы внутри. Разработку тянут двое, поддержать можно только багрепортом и словом.",
    ],
    [
      "Что с безопасностью аккаунта?",
      "Клиент не запрашивает логин и пароль, не шлёт телеметрию и не обращается к нашим серверам во время игры. Конфиги лежат локально в папке .minecraft/socket, их можно просто скопировать на другой ПК.",
    ],
    [
      "Как перенести настройки?",
      "Через ConfigProfiles: выгрузите профиль в файл и положите его в ту же папку на новой машине. Профили переносятся между сборками клиента.",
    ],
  ];

  const faqList = $("#faqList");
  if (faqList) {
    faqList.innerHTML = FAQ.map(
      ([q, a], i) => `
      <div class="q">
        <button class="q-head" aria-expanded="false" aria-controls="a${i}">
          ${q}<span class="pm" aria-hidden="true"></span>
        </button>
        <div class="q-body" id="a${i}"><div><p>${a}</p></div></div>
      </div>`
    ).join("");

    faqList.addEventListener("click", (e) => {
      const head = e.target.closest(".q-head");
      if (!head) return;
      const item = head.parentElement;
      const open = !item.classList.contains("open");

      $$(".q", faqList).forEach((q) => {
        q.classList.remove("open");
        $(".q-head", q).setAttribute("aria-expanded", "false");
      });

      if (open) {
        item.classList.add("open");
        head.setAttribute("aria-expanded", "true");
      }
    });
  }

  /* ---------- плавный скролл с учётом фиксированной навигации ---------- */
  document.addEventListener("click", (e) => {
    const a = e.target.closest('a[href^="#"]');
    if (!a) return;
    const href = a.getAttribute("href");
    if (href === "#") return;
    const t = $(href);
    if (!t) return;
    e.preventDefault();
    const offset =
      t.getBoundingClientRect().top + scrollY - (nav.offsetHeight + 18);
    scrollTo({ top: offset, behavior: reduced ? "auto" : "smooth" });
  });
})();
