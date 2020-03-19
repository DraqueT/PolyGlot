import os.path

# .. Useful stuff ..............................................................

application = 'PolyGlot.app'
appname = os.path.basename(application)

# .. Basics ....................................................................

# Volume format (see hdiutil create -help)
format = 'UDZO'

# Volume size
size = None

# Files to include
files = [ application ]

# Symlinks to create
symlinks = { 'Applications': '/Applications' }

# Where to put the icons
icon_locations = {
    appname:        (180, 170),
    'Applications': (480, 170)
}

# .. Window configuration ......................................................

# Background
background = 'packaging_files/mac/dmg-background.tiff'

# Window position in ((x, y), (w, h)) format
window_rect = ((100, 100), (660, 400))

# .. Icon view configuration ...................................................

icon_size = 160

# .. License configuration .....................................................

license = { 'licenses': { 'en_US': 'LICENSE.TXT' } }
