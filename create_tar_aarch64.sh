#!/bin/bash
pushd build
cp ../packaging_files/PolyGlot0.png image/
cp ../io.github.DraqueT.PolyGlot.desktop image/
cp ../io.github.DraqueT.PolyGlot.metainfo.xml image/
tar -C image/ -czvf image_aarch64.tar.gz .
popd
