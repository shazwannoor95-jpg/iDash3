import pathlib,json
root=pathlib.Path(__file__).resolve().parent;cases=[]
for p in sorted(list(root.glob('craft-home-*.json'))+list(root.glob('craft-fm-*.json'))):
 r=json.loads(p.read_text());checks={};c,j,d=r['clock'],r['prayer'],r['dock'];v=r['viewport']
 checks['unobstructed_app_focus']=r['window_focused'];checks['equal_cards']=r['card_equal'];checks['same_left_edge']=c['x']==j['x'];checks['14px_card_gap']=r['card_gap']==14;checks['cards_clear_dock']=j['y']+j['height']<=d['y'];checks['no_text_clipping']=not any(t['clipped'] for t in r['text']);checks['no_ellipsis']=not any(t['ellipsized'] for t in r['text']);checks['dock_inside_viewport']=d['x']>=v['x'] and d['x']+d['width']<=v['x']+v['width'] and d['y']+d['height']<=v['y']+v['height']
 actions=['Panel','Customize','Control Center']+(['Panel Mode','Panel Layout','Panel Close'] if r['case'].startswith('fm-') else [])
 row=[next(x for x in r['controls'] if x['description']==name) for name in actions];checks['toolbar_aligned']=len(set(x['y'] for x in row))==1;checks['toolbar_separated']=all(a['x']+a['width']<=b['x'] for a,b in zip(row,row[1:]));checks['toolbar_inside']=all(x['x']>=v['x'] and x['x']+x['width']<=v['x']+v['width'] for x in row)
 if 'fm' in r:
  fm,box=r['fm'],r['panel'];checks['fm_fills_panel']=fm['x']==box['x']+4 and fm['y']==box['y']+4 and fm['width']==box['width']-8 and fm['height']==box['height']-8
  presets=[x for x in r['controls'] if str(x['description']).startswith('FM channel ')];checks['ten_presets']=len(presets)==10;checks['presets_visible_and_tappable']=all(x['width']>=48 and x['height']>=48 and x['x']>=fm['x'] and x['x']+x['width']<=fm['x']+fm['width'] and x['y']+x['height']<=fm['y']+fm['height'] for x in presets)
 cases.append({'case':r['case'],'viewport':v,'clock':c,'prayer':j,'dock':d,'visible_text_views':len(r['text']),'checks':checks,'failed':[k for k,val in checks.items() if not val]})
result={'cases':cases,'checked_conditions':sum(len(x['checks']) for x in cases),'failures':sum(len(x['failed']) for x in cases)}
(root/'measured-summary.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result,indent=2))
