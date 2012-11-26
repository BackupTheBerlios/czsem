#!/bin/sh

if test ! -n "$TREEX_ROOT"
then
  TREEX_ROOT=`which treex | sed -e 's/bin\/treex$//'`
fi

if test ! -n "$TREEX_ONLINE"
then
  CURRENT_SCRIPT=`readlink -f $0`
  TREEX_ONLINE=`echo $CURRENT_SCRIPT | sed -e 's/\/[^\/]*$/\/treex_online.pl/'`
fi

 
echo "Running perl $TREEX_ONLINE $@"
echo "in directory: $TREEX_ROOT"

cd $TREEX_ROOT
perl $TREEX_ONLINE $@


