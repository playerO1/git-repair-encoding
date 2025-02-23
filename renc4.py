"""
Repair encoding by history file.

Required additional library `chardet`. You can install via: `pip3 install chardet`.
You can do not install it (or remove from code). But then you should use encoding in parameter.

(C) playerO1 (www.github.com/playerO1), 2025. Distributed under GNU GPL v3 license.
"""

class RepairText:
    W=7         # match window size
    E={'?','�'} # bad symbol for repair
    
    def check_line_on_E(self, s):
        return any(c in self.E for c in s)
    
    def cmp_line(self, str1, str2):
        if len(str1) != len(str2):
            return False
        for char1, char2 in zip(str1, str2):
            if char1 not in self.E and char2 not in self.E and char1 != char2:
                return False
        return True
    
    def search_match(self, lines_l:list, lines_r:list, idx_r:int):
        if (idx_r<0 or idx_r>len(lines_r)): raise BaseException('idx_r='+str(idx_r)+' out of range')
        match_w=[]
        for i in range(0,len(lines_l)):
            if not self.cmp_line(lines_r[idx_r], lines_l[i]):
                continue
            n=1
            r_r=min(self.W, min(len(lines_r)-idx_r,len(lines_l)-i))
            for j in range(0,r_r):
               if not self.cmp_line(lines_l[i+j],lines_r[idx_r+j]):
                   break
               n+=1
            r_l=min(min(self.W, idx_r),i)
            for j in range(0,r_l):
               if not self.cmp_line(lines_l[i-j],lines_r[idx_r-j]):
                   break
               n+=1
            if n>=self.W:
              match_w.append(i)
        return match_w
    
    def repair_lines(self, lines_l:list, lines_r:list):
        rep_n=0
        rep_err=0
        rep_missing=0
        for i in range(0,len(lines_r)):
            if self.check_line_on_E(lines_r[i]):
                in_l = self.search_match(lines_l, lines_r, i)
                if len(in_l)==1:
                    j=in_l[0]
                    if not self.check_line_on_E(lines_l[j]):
                        if self.cmp_line(lines_r[i],lines_l[j]):# additional check, versus error
                            lines_r[i]=lines_l[j]
                            rep_n=rep_n+1
                        else: # never, bad logic
                            print("Error, not match:",str(i),':',lines_r[i],' <> ',str(p2),':',lines_l[j])
                            rep_err=rep_err+1
                    else:
                        rep_missing=rep_missing+1
                elif len(in_l)>1:
                    rep_err=rep_err+1
                elif len(in_l)==0:
                  rep_missing=rep_missing+1
        return [rep_n, rep_missing, rep_err]

class RepairFile:
    repair_engine=RepairText()
    def checkenc(self, fn):
        import chardet
        with open(fn, 'rb') as f:
            data = f.read()
            encod = chardet.detect(data)
        confidence=round(encod['confidence']*100)
        encod = encod['encoding'] # {'encoding': 'utf-8', 'confidence': 0.99, 'language': ''}
        print(f"Read {fn} ({encod} {confidence}%)")
        return encod
    
    def repair_files(self, f_name1,f_name2, f1_enc=None,f2_enc=None):
        if None==f1_enc:
	        f1_enc=self.checkenc(f_name1) #'windows-1251'
        if None==f2_enc:
            f2_enc=self.checkenc(f_name2)# #'windows-1251' # todo with BOM utf+8, auto detect
        print('Read 2 files: ', f_name1,f_name2)
        f = open(f_name1, 'r', encoding=f1_enc) 
        lines1=f.readlines()
        f.close()
        f = open(f_name2, 'r', encoding=f2_enc)
        lines2=f.readlines()
        f.close()
        print('Process...')
        stat=self.repair_engine.repair_lines(lines1,lines2)
        print(f"   repair statistic \"{f_name2}\": rep_n={stat[0]}, rep_missing={stat[1]}, rep_err={stat[2]}")
        out_name=f_name2+'_out.txt'
        f = open(out_name, 'w', encoding=f2_enc)
        for s in lines2:
            f.write(s)
        f.close()
        print(f"Write {f_name1} ({f1_enc}) + {f_name2} ({f2_enc}) > {out_name} ({f2_enc})")
        return out_name

# --------------

def unit_test_0():
  r=RepairText()
  print('cmp_line test 1:',r.cmp_line('1','1')==True)
  print('cmp_line test 1:',r.cmp_line(' 2a',' 2a')==True)
  print('cmp_line test 1:',r.cmp_line(' a?',' ?x')==True)
  print('cmp_line test 1:',r.cmp_line('1','2')==False)
  print('cmp_line test 1:',r.cmp_line('12','1')==False)
  print('cmp_line test 1:',r.cmp_line('??','?')==False)
  print('cmp_line test 1:',r.cmp_line('?','??')==False)
  
def unit_test_1():
  r=RepairText()
  r.W=2 # window size
  left=['1','2',' 3',' 4', '5',' 67', ' 67', ' 67','8']
  right=['2','2',' 3',' 4', '5',' 67', ' 67', ' 67','-']
  match=r.search_match(left,right,2)
  print('search_match test first> ',str(match),' must be =2')
  expected=[[1],[1],[2],[3],[4],[5,6,7],[5,6,7],[5,6,7]]
  ok=True
  for i in range(len(expected)):
      match=r.search_match(left,right,i)
      exp=expected[i]
      print('search_match test> ',str(i),'result=',str(match),' expected=',str(exp))
      for p2 in match:
          if left[p2] != right[i]:
            print("Error not match:",str(i),':',right[i],' <> ',str(p2),':',left[p2])
            of=False
      ok = ok and match==exp
  print('search_match Test:',ok)

#unit_test_0()
#unit_test_1()
# --------------

if __name__ == "__main__":
    f_rep=RepairFile()
    print("Start")
    
    #f_rep.repair_files("first.sql", 'second.sql', 'utf-8','utf-8')
    #f_rep.repair_files("text1.txt", 'text2.txt', 'windows-1251','windows-1251')
    
    import sys, os
    if (len(sys.argv)==2) and ("--help"==sys.argv[1]):
        print("Need 2 or 3,4 arg: file_1 file_2 [encoding] [encoding 2].")
        exit(0)
    f1_enc,f2_enc = None, None #'windows-1251','windows-1251'
    if len(sys.argv)<=2:
        print("Need 2 or 3,4 arg: file_1 file_2 [encoding] [encoding 2].")
        raise BaseException("Need path in argument or list of files.")
    else: # len>=3
        file_1,file_2 = sys.argv[1],sys.argv[2]
        if len(sys.argv)==4:
            f1_enc,f2_enc=sys.argv[3],sys.argv[3]
        elif len(sys.argv)>=5:
            f1_enc,f2_enc=sys.argv[3],sys.argv[4]
    print(f"Start. Files: {file_1} ({f1_enc}) and {file_2} ({f2_enc})")
    f_rep.repair_files(file_1, file_2, f1_enc, f2_enc)
    
    print('End.')

# python3 renc4.py orig.txt broken.txt utf8 utf8
# for repair sequence do it 2 times:
#  > python3 renc4.py hist1.txt hist2.txt
#  > python3 renc4.py hist2.txt_out.txt hist3.txt
# take hist3.txt_out.txt
