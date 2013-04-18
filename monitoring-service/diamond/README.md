Installation
=====

### Installation/Building Dependencies 

- make
- rpm-build

### Core Dependencies

- CentOS or Ubuntu
- Python 2.4+
- python-configobj
- [Python Psutil](http://code.google.com/p/psutil/) for non linux system metrics


Debian Squeeze / Ubuntu Precise
====

- sudo apt-get install make pbuilder python-mock python-configobj python-support cdbs
- sudo dpkg -i diamond_*.deb
- sudo cp diamond.conf.example /etc/diamond/diamond.conf
- sudo /etc/init.d/diamond start
