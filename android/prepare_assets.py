"""Copy the tracker into assets/www and bundle Google Fonts so the app works offline."""
import os, re, shutil, urllib.request, hashlib, sys
HERE = os.path.dirname(os.path.abspath(__file__)); ROOT = os.path.dirname(HERE)
OUT = os.path.join(HERE, 'gen-assets', 'www'); FONTS = os.path.join(OUT, 'fonts')
CACHE = os.path.join(HERE, 'fonts-cache')
os.makedirs(FONTS, exist_ok=True); os.makedirs(CACHE, exist_ok=True)
UA = 'Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Mobile Safari/537.36'

html = open(os.path.join(ROOT, 'namaz-tracker.html'), encoding='utf-8').read()
m = re.search(r'<link href="(https://fonts\.googleapis\.com/css2\?[^"]+)" rel="stylesheet">', html)
css_url = m.group(1).replace('&amp;', '&')

def get(url):
    key = os.path.join(CACHE, hashlib.sha1(url.encode()).hexdigest())
    if not os.path.exists(key):
        req = urllib.request.Request(url, headers={'User-Agent': UA})
        with urllib.request.urlopen(req, timeout=60) as r, open(key, 'wb') as f: f.write(r.read())
    return open(key, 'rb').read()

try:
    css = get(css_url).decode()
    for i, u in enumerate(sorted(set(re.findall(r'url\((https://fonts\.gstatic\.com/[^)]+)\)', css)))):
        name = f'f{i}.woff2'
        open(os.path.join(FONTS, name), 'wb').write(get(u))
        css = css.replace(u, name)
    open(os.path.join(FONTS, 'fonts.css'), 'w', encoding='utf-8').write(css)
    html = html.replace(m.group(0), '<link href="fonts/fonts.css" rel="stylesheet">')
    html = re.sub(r'\s*<link rel="preconnect"[^>]*>', '', html)
    print('fonts bundled:', len(os.listdir(FONTS)) - 1, 'files')
except Exception as e:
    print('WARNING: fonts not bundled, app will use online fonts:', e, file=sys.stderr)

open(os.path.join(OUT, 'namaz-tracker.html'), 'w', encoding='utf-8').write(html)
shutil.copytree(os.path.join(ROOT, 'icons'), os.path.join(OUT, 'icons'), dirs_exist_ok=True)
