for javafile in org/maeden/simulator/*.java ;
do
    sed -i -e 's/\/\/\/\*maedengraphics/\/\*maedengraphics/ ; s/\/\/maedengraphics\*\//maedengraphics\*\//' $javafile;
done
