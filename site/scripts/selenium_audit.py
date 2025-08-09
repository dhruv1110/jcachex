#!/usr/bin/env python3
import os
import sys
import time
import argparse
from pathlib import Path

from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import ElementClickInterceptedException
from selenium.webdriver.common.by import By
from selenium.common.exceptions import NoSuchElementException, WebDriverException, ElementNotInteractableException


ROUTES = [
    '/',
    '/getting-started',
    '/examples',
    '/spring',
    '/performance',
    '/documentation',
    '/docs',
]

ASSERTIONS = {
    '/': [
        (By.XPATH, "//h1[contains(., 'High-performance Java caching')]")
    ],
    '/examples': [
        (By.XPATH, "//h1[contains(., 'JCacheX Examples')]")
    ],
    '/spring': [
        (By.XPATH, "//h1[contains(., 'Spring Boot Integration')]")
    ],
    '/performance': [
        (By.XPATH, "//h2[contains(., 'JCacheX Performance Benchmarks')]")
    ],
    '/documentation': [
        (By.XPATH, "//h1[contains(., 'JCacheX Documentation')]|//h2[contains(., 'Documentation')]")
    ],
}


def _log(msg: str):
    print(msg, flush=True)


def click_all_clickables(driver: webdriver.Chrome, base_url: str) -> int:
    issues = 0
    clickables = driver.find_elements(By.CSS_SELECTOR, "#main-content a[href], #main-content button")
    _log(f"  - Found {len(clickables)} clickables in main content")
    max_to_click = min(len(clickables), 30)
    for idx in range(max_to_click):
        el = clickables[idx]
        try:
            # Only interact with visible and enabled elements
            if not el.is_displayed() or not el.is_enabled():
                _log(f"    · Skipping idx {idx}: not displayed or not enabled")
                continue
            size = el.size or {}
            if size.get('width', 0) == 0 or size.get('height', 0) == 0:
                _log(f"    · Skipping idx {idx}: zero size")
                continue
            # Ensure in view
            driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", el)
            time.sleep(0.1)
            # If anchor without href and without onClick handlers, mark issue
            tag = el.tag_name.lower()
            label = (el.text or '').strip()
            if tag == 'a':
                href = el.get_attribute('href')
                if not href:
                    _log(f"    · Missing href on anchor idx {idx}; counting as issue")
                    issues += 1
                    continue
                # Avoid opening external links during audit
                if href.startswith('http') and not href.startswith(base_url):
                    _log(f"    · Skipping external link idx {idx}: {href}")
                    continue
                if el.get_attribute('target') == '_blank':
                    _log(f"    · Skipping target=_blank link idx {idx}: {href}")
                    continue
                if href.endswith('#') or href.rsplit('#', 1)[0] == '':
                    _log(f"    · Skipping hash/self link idx {idx}: {href}")
                    continue
            try:
                original_url = driver.current_url
                _log(f"    · Clicking idx {idx}: <{tag}> '{label[:40]}'")
                el.click()
                time.sleep(0.1)
                # If navigation occurred, return safely to original page without relying on history
                if driver.current_url != original_url and driver.current_url.startswith(base_url):
                    _log(f"      navigated to {driver.current_url}, returning to {original_url}")
                    driver.get(original_url)
                time.sleep(0.1)
            except (ElementNotInteractableException, ElementClickInterceptedException):
                # Fallback: keyboard activation
                try:
                    driver.execute_script("arguments[0].focus();", el)
                    _log(f"      click intercepted; trying keyboard activation on idx {idx}")
                    el.send_keys("\n")
                    time.sleep(0.1)
                    # If navigation occurred, return to original
                    if driver.current_url != original_url and driver.current_url.startswith(base_url):
                        _log(f"      navigated (keyboard) to {driver.current_url}, returning to {original_url}")
                        driver.get(original_url)
                    time.sleep(0.1)
                except Exception:
                    _log(f"      failed to activate idx {idx}")
                    issues += 1
        except Exception as e:
            _log(f"    · Error on idx {idx}: {type(e).__name__}: {e}")
            issues += 1
    return issues


def click_all_sidebar_items(driver: webdriver.Chrome) -> int:
    """If a sidebar exists, expand and click through all items to scroll to sections."""
    issues = 0
    try:
        # Expand any collapsed parent items by clicking expand icons
        expanders = driver.find_elements(By.CSS_SELECTOR, ".MuiDrawer-paper .MuiListItemButton-root svg[data-testid='ExpandMoreIcon']")
        for exp in expanders:
            try:
                driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", exp)
                time.sleep(0.05)
                exp.click()
                time.sleep(0.05)
            except Exception:
                continue
        # Click all sidebar list items to trigger in-page scroll
        items = driver.find_elements(By.CSS_SELECTOR, ".MuiDrawer-paper .MuiListItemButton-root")
        _log(f"  - Sidebar items visible: {len(items)}")
        for it in items:
            try:
                if not it.is_displayed():
                    continue
                driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", it)
                time.sleep(0.05)
                it.click()
                time.sleep(0.05)
            except Exception:
                _log("    · Sidebar item click failed; counting as issue")
                issues += 1
    except Exception:
        # No sidebar present
        pass
    return issues


def audit(base_url: str, out_dir: Path) -> int:
    out_dir.mkdir(parents=True, exist_ok=True)
    chrome_opts = Options()
    chrome_opts.add_argument('--headless=new')
    chrome_opts.add_argument('--no-sandbox')
    chrome_opts.add_argument('--disable-gpu')
    chrome_opts.add_argument('--window-size=1440,900')
    # Enable browser console logs
    try:
        chrome_opts.set_capability('goog:loggingPrefs', {'browser': 'ALL'})
    except Exception:
        pass
    driver = None
    failures = 0
    try:
        _log(f"Starting Chrome WebDriver against {base_url}")
        driver = webdriver.Chrome(options=chrome_opts)
        try:
            driver.set_page_load_timeout(12)
        except Exception:
            pass
        for route in ROUTES:
            url = base_url.rstrip('/') + route
            _log(f"\nNavigating to: {url}")
            driver.get(url)
            # wait for root and main content to appear
            try:
                WebDriverWait(driver, 5).until(EC.presence_of_element_located((By.ID, 'root')))
            except Exception:
                pass
            try:
                WebDriverWait(driver, 5).until(EC.presence_of_element_located((By.ID, 'main-content')))
            except Exception:
                time.sleep(0.5)
            # capture console errors
            try:
                logs = driver.get_log('browser')
            except Exception:
                logs = []
            errors = [l for l in logs if l.get('level') in ('SEVERE', 'ERROR')]
            _log(f"  Console errors: {len(errors)}")
            if errors:
                failures += 1
                (out_dir / 'console.log').write_text(
                    (out_dir / 'console.log').read_text() + f"\n\n# {route}\n" + "\n".join(str(e) for e in errors)
                    if (out_dir / 'console.log').exists() else
                    f"# {route}\n" + "\n".join(str(e) for e in errors)
                )

            # assert key elements
            for by, sel in ASSERTIONS.get(route, []):
                try:
                    driver.find_element(by, sel)
                except NoSuchElementException:
                    failures += 1
                    _log(f"  Missing expected element on {route}: {sel}")

            # clickability audit (main)
            issues_main = click_all_clickables(driver, base_url)
            failures += issues_main
            _log(f"  Click audit (main) issues: {issues_main}")
            # clickability audit (sidebar navigation)
            issues_sidebar = click_all_sidebar_items(driver)
            failures += issues_sidebar
            _log(f"  Click audit (sidebar) issues: {issues_sidebar}")

            # screenshot
            safe = route.strip('/').replace('/', '_') or 'home'
            shot_path = out_dir / f"{safe}.png"
            driver.save_screenshot(str(shot_path))
            _log(f"  Saved screenshot: {shot_path}")

    except WebDriverException as e:
        _log(f"WebDriver error: {e}")
        failures += 1
    finally:
        if driver:
            driver.quit()

    return failures


def main():
    parser = argparse.ArgumentParser(description='Selenium UI audit for JCacheX site')
    parser.add_argument('--base-url', default='http://localhost:3001', help='Base URL where site is served')
    parser.add_argument('--out', default='selenium_screens', help='Output directory for screenshots/logs')
    args = parser.parse_args()

    out_dir = Path(args.out)
    _log(f"Running audit against {args.base_url}; output: {out_dir}")
    failures = audit(args.base_url, out_dir)
    if failures:
        _log(f"Audit completed with {failures} issue(s). See {out_dir} for details.")
        sys.exit(1)
    _log(f"Audit passed. Screenshots in {out_dir}.")


if __name__ == '__main__':
    main()


