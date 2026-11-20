#!/usr/bin/env python3
"""Build TK An Najah dari src/index.html.

Hasil:
  app/src/main/assets/index.html  (APK)
  web/index.html                  (GitHub Pages)
Pemakaian (dari folder repo):
  cd tools && npm install && cd ..
  python3 tools/build.py            # versi Firebase sungguhan
  python3 tools/build.py --mock     # versi uji -> /tmp/test_cloud.html (Firebase tiruan)
src/index.html sendiri (tanpa Firebase) dipakai juga sebagai versi pribadi di Claude.
"""
import base64, json, os, re, subprocess, sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
T = os.path.join(ROOT, 'tools')
NM = os.path.join(T, 'node_modules')
MOCK = '--mock' in sys.argv

FIREBASE_CONFIG = {
    "apiKey": "AIzaSyCFdowPDw0gpEwKxsrMsj2FMSWvjfcXk70",
    "authDomain": "tk-an-najah.firebaseapp.com",
    "projectId": "tk-an-najah",
    "storageBucket": "tk-an-najah.firebasestorage.app",
    "messagingSenderId": "1038932143963",
    "appId": "1:1038932143963:web:01bde1f285c57ed2e1747e",
}
PAGES = "https://qois2020.github.io/TK-An-Najah"
APK_URL = "https://github.com/qois2020/TK-An-Najah/releases/latest/download/TKAnNajah.apk"

def fonts():
    lat = 'U+0000-00FF,U+0131,U+0152-0153,U+02BB-02BC,U+02C6,U+02DA,U+02DC,U+0304,U+0308,U+0329,U+2000-206F,U+20AC,U+2122,U+2191,U+2193,U+2212,U+2215,U+FEFF,U+FFFD'
    ext = 'U+0100-02BA,U+02BD-02C5,U+02C7-02CC,U+02CE-02D7,U+02DD-02FF,U+0304,U+0308,U+0329,U+1D00-1DBF,U+1E00-1E9F,U+1EF2-1EFF,U+2020,U+20A0-20AB,U+20AD-20C0,U+2113,U+2C60-2C7F,U+A720-A7FF'
    css = ''
    for w in (500, 600, 700, 800):
        for sub, rng in (('latin', lat), ('latin-ext', ext)):
            f = os.path.join(NM, '@fontsource/baloo-2/files', f'baloo-2-{sub}-{w}-normal.woff2')
            b = base64.b64encode(open(f, 'rb').read()).decode()
            css += f"@font-face{{font-family:'Baloo 2';font-weight:{w};font-style:normal;font-display:swap;unicode-range:{rng};src:url(data:font/woff2;base64,{b}) format('woff2')}}\n"
    for w in (400, 700):
        f = os.path.join(NM, '@fontsource/amiri/files', f'amiri-arabic-{w}-normal.woff2')
        b = base64.b64encode(open(f, 'rb').read()).decode()
        css += f"@font-face{{font-family:'Amiri';font-weight:{w};font-style:normal;font-display:swap;src:url(data:font/woff2;base64,{b}) format('woff2')}}\n"
    return css

def firebase_lib():
    if MOCK:
        return open(os.path.join(T, 'mock.js')).read()
    out = os.path.join(T, 'fb.bundle.js')
    subprocess.check_call([os.path.join(NM, '.bin', 'esbuild'), os.path.join(T, 'fb.js'), '--bundle', '--minify',
                           '--format=iife', '--target=es2019', '--outfile=' + out], cwd=T)
    return open(out).read()

s = open(os.path.join(ROOT, 'src', 'index.html')).read()
s = re.sub(r'<link rel="preconnect"[^>]*>\n', '', s)
s = re.sub(r'<link href="https://fonts.googleapis.com[^>]*>\n', '', s)
s = s.replace('<style>\n', '<style>\n' + fonts(), 1)
head = '''<meta name="theme-color" content="#14b8a6">
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-title" content="TK An Najah">
<meta name="apple-mobile-web-app-status-bar-style" content="default">
<link rel="manifest" href="manifest.json">
<link rel="icon" href="icon-192.png">
<link rel="apple-touch-icon" href="apple-touch-icon.png">
'''
s = s.replace('<title>', head + '<title>', 1)
cfg = ('<script>window.FB_CONFIG=' + json.dumps(FIREBASE_CONFIG) +
       ';window.APP_BUILD="__BUILD__";window.UPDATE_URL="' + PAGES + '/version.json";window.APK_URL="' + APK_URL + '";</script>\n')
inj = cfg + '<script>' + firebase_lib().replace('</script', '<\\/script') + '</script>\n'
i = s.index('<script>\n/* ================= DATA: DOA')
s = s[:i] + inj + s[i:]
outs = ['/tmp/test_cloud.html'] if MOCK else [os.path.join(ROOT, 'app/src/main/assets/index.html'), os.path.join(ROOT, 'web/index.html')]
for o in outs:
    os.makedirs(os.path.dirname(o), exist_ok=True)
    open(o, 'w').write(s)
print(f'{len(s)//1024} KB ->', ', '.join(outs))
