rem see http://msdn.microsoft.com/en-us/library/aa367988.aspx for details
echo Got prodcut code as: %1
START %windir%\system32\msiexec /i %1 /qf
