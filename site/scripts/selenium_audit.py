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


def click_all_clickables(driver: webdriver.Chrome, base_url: str) -> int:
    issues = 0
    clickables = driver.find_elements(By.CSS_SELECTOR, "#main-content a[href], #main-content button")
    max_to_click = min(len(clickables), 30)
    for idx in range(max_to_click):
        el = clickables[idx]
        try:
            # Only interact with visible and enabled elements
            if not el.is_displayed() or not el.is_enabled():
                continue
            size = el.size or {}
            if size.get('width', 0) == 0 or size.get('height', 0) == 0:
                continue
            # Ensure in view
            driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", el)
            time.sleep(0.1)
            # If anchor without href and without onClick handlers, mark issue
            tag = el.tag_name.lower()
            if tag == 'a':
                href = el.get_attribute('href')
                if not href:
                    issues += 1
                    continue
                # Avoid opening external links during audit
                if href.startswith('http') and not href.startswith(base_url):
                    continue
                if el.get_attribute('target') == '_blank':
                    continue
                if href.endswith('#') or href.rsplit('#', 1)[0] == '':
                    continue
            try:
                original_url = driver.current_url
                el.click()
                time.sleep(0.1)
                # If navigation occurred, return safely to original page without relying on history
                if driver.current_url != original_url and driver.current_url.startswith(base_url):
                    driver.get(original_url)
                time.sleep(0.1)
            except (ElementNotInteractableException, ElementClickInterceptedException):
                # Fallback: keyboard activation
                try:
                    driver.execute_script("arguments[0].focus();", el)
                    el.send_keys("\n")
                    time.sleep(0.1)
                    # If navigation occurred, return to original
                    if driver.current_url != original_url and driver.current_url.startswith(base_url):
                        driver.get(original_url)
                    time.sleep(0.1)
                except Exception:
                    issues += 1
        except Exception:
            issues += 1
    return issues


def audit(base_url: str, out_dir: Path) -> int:
    out_dir.mkdir(parents=True, exist_ok=True)
    chrome_opts = Options()
    chrome_opts.add_argument('--headless=new')
    chrome_opts.add_argument('--no-sandbox')
    chrome_opts.add_argument('--disable-gpu')
    chrome_opts.add_argument('--window-size=1440,900')
    driver = None
    failures = 0
    try:
        driver = webdriver.Chrome(options=chrome_opts)
        try:
            driver.set_page_load_timeout(12)
        except Exception:
            pass
        for route in ROUTES:
            url = base_url.rstrip('/') + route
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
                    print(f"Missing expected element on {route}: {sel}")

            # clickability audit
            failures += click_all_clickables(driver, base_url)

            # screenshot
            safe = route.strip('/').replace('/', '_') or 'home'
            driver.save_screenshot(str(out_dir / f"{safe}.png"))

    except WebDriverException as e:
        print(f"WebDriver error: {e}")
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
    failures = audit(args.base_url, out_dir)
    if failures:
        print(f"Audit completed with {failures} issue(s). See {out_dir} for details.")
        sys.exit(1)
    print(f"Audit passed. Screenshots in {out_dir}.")


if __name__ == '__main__':
    main()


