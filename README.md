# <ins>lookForSelect</ins>

!['lookForSelect'](https://github.com/markcapella/lookForSelect/blob/main/lookForSelect.png)
!['lookForSelect'](https://github.com/markcapella/lookForSelect/blob/main/assets/screenshot.png)


## <ins>Description</ins>

###    Quickly look for files on your system, using (optionally), a string portion
###    of the file name, the location to be searched, and anything the file name ends with.

    Results are displayed in a GUI ListView where a double click will XDG-OPEN
    the selected item.


## <ins>Installation</ins>

###    CD into source repo.

    make
    make install

    make run

    make uninstall
    make clean

## <ins>Installed Usage</ins>

###    lookForSelect string target endsWith
###    lfs string target endsWith


    lfs

    lfs foo
    lfs foo ~
    lfs foo ~ .cpp

    lfs "" /
    lfs "" "~" "/.git"

    NOTE: If the target is the root dir "/", the user's HOME folder
          is assumed un-interesting and will be skipped, perhaps
          requiring s second scan for an entire system review.


## markcapella@twcny.rr.com Rocks !

    Yeah I do.
