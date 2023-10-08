
# *****************************************************
# Variables to control Makefile operation.

OPENJFX = /usr/share/openjfx/lib
# OPENJFX = /home/$USER/jfx/build/sdk/lib

JCOMPILER = javac

JFLAGS = --module-path $(OPENJFX) \
	     --add-modules javafx.controls,javafx.swing,javafx.media


# ****************************************************
# Targets needed to build the executable from the source folder.

lookForSelect: lookForSelect.java
	@echo
	@echo "Build starts ..."
	@echo

	@if [ ! -d $(OPENJFX) ]; then \
		echo "Error! The openjfx package is not installed, but is required."; \
		echo "   try 'sudo apt install openjfx', then re-run this make."; \
		echo ""; \
		exit 1; \
	fi

	$(JCOMPILER) $(JFLAGS) lookForSelect.java

	@echo
	@echo "Build Done !"
	@echo

# ****************************************************
# Target needed to install the executable.

install: lookForSelect
ifneq ($(shell id -u), 0)
	@echo "You must be root to perform this action. Please re-run with:"
	@echo "   sudo make install"
	@echo
	@exit 1;
endif

	@echo
	@echo "Install: starts ..."
	@echo

	cp 'lfs' /usr/local/bin/
	chmod +x /usr/local/bin/lfs
	@echo

	mkdir -p /usr/local/lookForSelect
	cp *.class /usr/local/lookForSelect
	cp README.md  /usr/local/lookForSelect
	cp 'lookForSelect.png' /usr/local/lookForSelect
	@echo

	sudo -u ${SUDO_USER} \
		rm -rf /home/${SUDO_USER}/.java/.userPrefs/lookForSelect
	@echo

	@echo "Install Done !"
	@echo

# ****************************************************
# Target needed to uninstall the executable.

uninstall:
ifneq ($(shell id -u), 0)
	@echo "You must be root to perform this action. Please re-run with:"
	@echo "   sudo make uninstall"
	@echo
	@exit 1;
endif

	@echo
	@echo "Uninstall: starts ..."
	@echo

	rm -f /usr/local/bin/lfs
	@echo

	rm -rf /usr/local/lookForSelect
	@echo

	sudo -u ${SUDO_USER} \
		rm -rf /home/${SUDO_USER}/.java/.userPrefs/lookForSelect
	@echo

	@echo "Uninstall Done !"
	@echo

# ****************************************************
# Target needed to clean the source folder for a fresh make.

clean:
	@echo
	@echo "Clean: starts ..."
	@echo

	rm -f *.class

	rm -rf ~/.java/.userPrefs/lookForSelect

	@echo
	@echo "Clean Done !"
	@echo
