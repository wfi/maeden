for javafile in *.java ;
do
  cat $javafile | sed -e 's/\/\/\/\*maedengraphics/\/\*maedengraphics/' | sed -e 's/\/\/maedengraphics\*\//maedengraphics\*\//' > $javafile.tmp;
  mv -f $javafile.tmp $javafile; 
  done
