import requests

html = requests.get("https://go.illinois.edu/COVIDTestingData", allow_redirects=True)
f = open("x.html", "w+")
f.write(html.text)
f.close()
cookies = html.history[-2].headers["Set-Cookie"]
html = requests.get("https://splunk-public.machinedata.illinois.edu/en-US/static/@9451E87DE473A1D32F262FEFC4B778E934F0CDE7AF267713D45A3A8343323BBA/build/pages/dark/dashboard.js", allow_redirects=True, headers={"Set-Cookie": cookies})
f = open("x.html", "w+")
f.write(html.text)
f.close()
print(html.headers)
print([i.headers for i in html.history])

# from requests_html import HTMLSession
#
# session = HTMLSession()
#
# r = session.get("https://splunk-public.machinedata.illinois.edu/en-US/static/@9451E87DE473A1D32F262FEFC4B778E934F0CDE7AF267713D45A3A8343323BBA/build/pages/dark/dashboard.js")
#
#
# r.html.render()  # this call executes the js in the page

# session_id_8000=a9b9f6a20c9c5d01f47d40ab8316a866fe4d0f6f;

# from http.cookiejar import CookieJar
# import urllib, sys
#
# def doIt(uri):
#     cj = CookieJar()
#     opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cj))
#     page = opener.open(uri)
#     page.addheaders = [('User-agent', 'Mozilla/5.0')]
#     print(page.read())

# doIt("https://go.illinois.edu/COVIDTestingData")

# splunkd_8000=4LMgcc4JQuj4Fbja7l4KJRK9fvoELBQeHdtZB3_4kvct9kGS8O8x^TDG7ydJlybCFJovEypc1FpK^x9Iv1iqppKM5vceU27NYv5tgKaHpRUhDnof7P7^xhAAsJBlcbijecw^uGEzOoL; Path=/; Secure; HttpOnly; Max-Age=3600; Expires=Wed, 07 Oct 2020 01:12:53 GMT, splunkweb_csrf_token_8000=16132046137017909974; Path=/; Secure; Max-Age=157680000; Expires=Mon, 06 Oct 2025 00:12:53 GMT'
