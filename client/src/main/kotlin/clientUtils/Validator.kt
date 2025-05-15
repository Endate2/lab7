package clientUtils

class Validator {
    companion object {
        fun verifyArgs (size:Int, args: List<String>):Boolean {
            return args.size == size
        }

    }

}