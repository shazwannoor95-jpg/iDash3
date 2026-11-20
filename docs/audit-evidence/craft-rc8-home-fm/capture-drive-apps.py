# Workspace replay helper: choose a fresh root output directory before rerunning.
import subprocess,time,json,pathlib,sys
root=pathlib.Path('/workspace/craft-audits/rc8-pages-final');root.mkdir(exist_ok=True)
project='/workspace/craft-audits/idash3-rc8-ui.cany'
def run(args,timeout=45,check=True):return subprocess.run(args,capture_output=True,timeout=timeout,check=check)
def adb(*args,**kw):return run(['adb','-s','emulator-5554',*args],**kw)
def craft(command,params,out):
 r=run(['craft-anything-cli','--deep','--allow-run','--project',project,command,'--json',json.dumps(params)],timeout=45,check=False)
 out.write_bytes(r.stdout);data=json.loads(r.stdout)
 if r.returncode or 'error' in data:raise RuntimeError(str(data)[:1200])
 return data
end=time.monotonic()+160
while time.monotonic()<end:
 try:
  if adb('shell','getprop','sys.boot_completed',timeout=10).stdout.strip()==b'1':break
 except Exception:pass
 time.sleep(1)
else:raise RuntimeError('emulator startup timeout')
adb('shell','wm','size','1024x600');adb('shell','wm','density','160');adb('shell','settings','put','secure','immersive_mode_confirmations','confirmed')
adb('install','-r','-t','/workspace/iDash3/dist/iDash4-0.6.0-rc8-debug.apk',timeout=60)
adb('install','-r','-t','/workspace/iDash3/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk',timeout=60)
adb('shell','run-as','com.shazwan.idash3','rm','-f','files/craft-ready',check=False)
log=(root/'instrumentation.txt').open('wb');process=subprocess.Popen(['adb','-s','emulator-5554','shell','am','instrument','-w','-r','com.shazwan.idash3.test/com.shazwan.idash3.CraftPagesAudit'],stdout=log,stderr=subprocess.STDOUT)
try:
 for case in ['apps-1024x600','drive-1024x600','apps-800x480','drive-800x480']:
  end=time.monotonic()+130
  while time.monotonic()<end:
   r=adb('exec-out','run-as','com.shazwan.idash3','cat','files/craft-ready',check=False)
   if r.stdout.decode(errors='ignore').strip()==case:break
   if process.poll() is not None:raise RuntimeError('instrumentation exited: '+(root/'instrumentation.txt').read_text())
   time.sleep(.5)
  else:raise RuntimeError('ready timeout '+case)
  shot=craft('adb-screenshot-take',{'serial':'emulator-5554','output_directory':str(root/(case+'-screen'))},root/(case+'-screenshot-evidence.json'))
  metrics=craft('adb-file-download',{'serial':'emulator-5554','device_path':'/sdcard/Android/data/com.shazwan.idash3/files/craft-'+case+'.json','output_directory':str(root/(case+'-metrics'))},root/(case+'-metrics-evidence.json'))
  adb('shell','run-as','com.shazwan.idash3','touch','files/craft-next-'+case)
  print(case,'captured',flush=True)
 process.wait(timeout=40);log.close();text=(root/'instrumentation.txt').read_text();assert 'INSTRUMENTATION_CODE: -1' in text and 'stream=PASS:' in text,text
 print(text)
finally:
 if process.poll() is None:process.terminate()
