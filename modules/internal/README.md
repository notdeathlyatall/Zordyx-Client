# Zordyx Client Internal

`dependsOn("*")`

This module contains the actual Zordyx Client UI implementation, along with other internal things required for Zordyx Client to work. It is fully internal and not part of the public API. While you may be able to use it, this is not recommended and is volatile.

This package exists purely for code that is internal, dependant on other parts of the API, but is not 
dependant on Minecraft itself, so to improve build times and navigability of the codebase, this is used.
> As mentioned above, unlike other API packages, this is not found under `org.polyfrost.oneconfig.api.` but instead `org.polyfrost.oneconfig.internal` due to its internal nature.