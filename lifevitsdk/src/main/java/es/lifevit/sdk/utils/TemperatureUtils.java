package es.lifevit.sdk.utils;


/**
 * Created by aescanuela on 11/4/16.
 */
public class TemperatureUtils {

    private static int[] temperLowDataArray = {
            666, 786, 886, 986, 1086, 1186, 1286, 1386, 1486, 1586, 1686, 1786,
            1886, 1986, 2086, 2186, 2286, 2386, 2486, 2586, 2686, 2782, 2883,
            2982, 3084, 3185, 3288, 3388, 3487};
    private static int[] temperDataArray = {
            3487, 3491, 3495, 3500, 3504, 3508, 3512, 3515, 3519, 3522, 3526,
            3530, 3534, 3539, 3543, 3547, 3551, 3555, 3559, 3563, 3567, 3571,
            3575, 3578, 3582, 3586, 3590, 3594, 3597, 3601, 3605, 3610, 3614,
            3619, 3623, 3628, 3632, 3635, 3639, 3642, 3646, 3650, 3655, 3659,
            3664, 3668, 3672, 3676, 3680, 3684, 3688, 3692, 3697, 3701, 3706,
            3710, 3714, 3718, 3721, 3725, 3729, 3733, 3737, 3741, 3745, 3749,
            3753, 3758, 3762, 3767, 3771, 3775, 3779, 3784, 3788, 3792, 3796,
            3800, 3805, 3809, 3813, 3817, 3821, 3825, 3829, 3833, 3837, 3841,
            3846, 3850, 3854, 3858, 3862, 3867, 3871, 3875, 3880, 3884, 3889,
            3893, 3898, 3902, 3906, 3911, 3915, 3919, 3923, 3927, 3932, 3936,
            3940, 3944, 3948, 3952, 3956, 3960, 3964, 3969, 3973, 3978, 3982,
            3987, 3991, 3996, 4000, 4005, 4009, 4013, 4018, 4022, 4026, 4030,
            4034, 4039, 4043, 4047, 4052, 4056, 4061, 4065, 4070, 4074, 4079,
            4083, 4088, 4092, 4096, 4100, 4105, 4109, 4113, 4117, 4121, 4126,
            4130, 4134, 4138, 4142, 4147, 4151, 4155, 4160, 4165, 4169, 4174,
            4179, 4183, 4188, 4192, 4197, 4201, 4206, 4211, 4215, 4220, 4225,
            4229, 4233, 4236, 4240, 4244, 4248, 4253, 4257, 4262, 4266, 4271,
            4276, 4281, 4286, 4291, 4296, 4301, 4305, 4310, 4315, 4319, 4324,
            4328, 4333, 4337, 4341, 4345, 4350, 4354, 4358, 4362, 4367, 4371,
            4376, 4380, 4385, 4389, 4394, 4398, 4403, 4408, 4413, 4419, 4424,
            4429, 4433, 4438, 4442, 4447, 4451, 4455, 4459, 4464, 4468, 4472,
            4476, 4481, 4485, 4490, 4494, 4499, 4504, 4509, 4514, 4519, 4524,
            4529, 4534, 4539, 4544, 4549, 4554, 4558, 4563, 4568, 4572, 4576,
            4581, 4585, 4589, 4594, 4599, 4603, 4608, 4613, 4617, 4622, 4626,
            4631, 4635, 4640, 4645, 4649, 4654, 4659, 4664, 4669, 4674, 4679,
            4684, 4689, 4694, 4699, 4704, 4709, 4713, 4717, 4722, 4726, 4730,
            4735, 4740, 4746, 4751, 4756, 4760, 4765, 4769, 4774, 4778, 4783,
            4788, 4792, 4797, 4802, 4807, 4812, 4818, 4823, 4828, 4833, 4839,
            4844, 4850, 4855, 4860, 4864, 4869, 4873, 4878, 4883, 4887, 4892,
            4896, 4901, 4906, 4911, 4916, 4921, 4926, 4931, 4936, 4942, 4947,
            4952, 4957, 4961, 4966, 4970, 4975, 4980, 4986, 4991, 4997, 5002,
            5007, 5011, 5016, 5020, 5025, 5030, 5035, 5039, 5044, 5049, 5054,
            5060, 5065, 5071, 5076, 5081, 5085, 5090, 5094, 5099, 5104, 5109,
            5115, 5120, 5125, 5130, 5135, 5139, 5144, 5149, 5154, 5159, 5165,
            5170, 5175, 5180, 5185, 5191, 5196, 5201, 5206, 5211, 5216, 5221,
            5226, 5231, 5237, 5242, 5248, 5253, 5258, 5263, 5267, 5272, 5277,
            5282, 5287, 5293, 5298, 5303};
    private static int[] temperHighDataArray = {
            5303, 5441, 5574, 5712, 5855, 5996, 6136};
    private static int temperLowDataArraySize = temperLowDataArray.length;
    private static int temperDataArraySize = temperDataArray.length;
    private static int temperHighDataArraySize = temperHighDataArray.length;
    private static int[] envTemperArray = {78, 81, 83, 86, 88, 91, 94, 97,
            100, 103, 106, 109, 112, 115, 119, 122, 126, 129, 133, 137, 141,
            145, 149, 153, 157, 162, 166, 171, 175, 180, 185, 190, 195, 201,
            206, 211, 217, 223, 229, 235, 241, 247, 253, 260, 267, 273, 280,
            287, 295, 302, 310, 317, 325, 333, 341, 350, 358, 367, 375, 384,
            394, 403, 412, 422, 432, 442, 452, 463, 473, 484, 495, 506, 517,
            529, 541, 553, 565, 577, 590, 602, 615, 629, 642, 656, 669, 683,
            698, 712, 727, 742, 757, 772, 788, 804, 820, 836, 853, 869, 886,
            904, 921, 939, 957, 975, 993, 1012, 1031, 1050, 1069, 1089, 1109,
            1129, 1149, 1170, 1190, 1211, 1233, 1254, 1276, 1298, 1320, 1342,
            1365, 1388, 1411, 1434, 1458, 1482, 1506, 1530, 1554, 1579, 1604,
            1629, 1654, 1680, 1706, 1732, 1758, 1784, 1811};
    private static int envTemperArraySize = envTemperArray.length;
    private static float envBaseTemper = -20.0F;
    private static float envDeltaTemper = 0.5F;

    public static float getTemperatureInDegreesFromRaw(int intValue) {
        if (intValue <= temperLowDataArray[0]) {
            return (float) (30.0D - 0.5D * (
                    temperLowDataArray[0] - intValue) / (
                    temperLowDataArray[1] - temperLowDataArray[0]));
        }
        if (intValue < temperDataArray[0]) {
            for (int i = 0; i < temperLowDataArraySize; i++) {
                if (intValue < temperLowDataArray[i]) {
                    if (i == 0) {
                        return 20.0F;
                    }
                    return (float) (20.0D + 0.5D * (i - 1) + 0.5D * (
                            intValue - temperLowDataArray[(i - 1)]) / (
                            temperLowDataArray[i] - temperLowDataArray[(i - 1)]));
                }
            }
        } else {
            if (intValue >= temperHighDataArray[(temperHighDataArraySize - 1)]) {
                return (float) (45.0D + 0.5D * (
                        intValue - temperHighDataArray[(temperHighDataArraySize - 1)]) / (
                        temperHighDataArray[(temperHighDataArraySize - 1)] - temperHighDataArray[(temperHighDataArraySize - 2)]));
            }
            if (intValue >= temperHighDataArray[0]) {
                for (int i = 0; i < temperHighDataArraySize; i++) {
                    if (intValue < temperHighDataArray[i]) {
                        if (i == 0) {
                            return 42.0F;
                        }
                        return (float) (42.0D + 0.5D * (i - 1) + 0.5D * (
                                intValue - temperHighDataArray[(i - 1)]) / (
                                temperHighDataArray[i] - temperHighDataArray[(i - 1)]));
                    }
                }
            } else {
                for (int i = 0; i < temperDataArraySize; i++) {
                    if (intValue < temperDataArray[i]) {
                        if (i == 0) {
                            return 34.0F;
                        }
                        return (float) (34.0D + 0.02D * (i - 1) + 0.02D * (
                                intValue - temperDataArray[(i - 1)]) / (
                                temperDataArray[i] - temperDataArray[(i - 1)]));
                    }
                }
            }
        }
        return 30.0F;
    }

    public static float getEnvironmentTemperInDegreesFromRaw(int value) {
        if (value < envTemperArray[0]) {
            return envBaseTemper - envDeltaTemper * (envTemperArray[0] - value) / (
                    envTemperArray[1] - envTemperArray[0]);
        }
        if (value > envTemperArray[(envTemperArraySize - 1)]) {
            return envBaseTemper +
                    envDeltaTemper * (
                            envTemperArraySize - 1) +
                    envDeltaTemper * (
                            value - envTemperArray[(envTemperArraySize - 1)]) / (
                            envTemperArray[(envTemperArraySize - 1)] - envTemperArray[(envTemperArraySize - 2)]);
        }
        for (int i = 0; i < envTemperArraySize; i++) {
            if (value < envTemperArray[i]) {
                if (i == 0) {
                    return envBaseTemper;
                }
                return envBaseTemper + envDeltaTemper * (i - 1) +
                        envDeltaTemper * (value - envTemperArray[(i - 1)]) / (
                                envTemperArray[i] - envTemperArray[(i - 1)]);
            }
        }
        return -100.0F;
    }


    public static float celsiusToFahrenheit(double celsiusTemp) {
        return (float) celsiusTemp * 9.0f / 5.0f + 32.0f;
    }


    public static float fahrenheitToCelsius(double fahrenheitTemp) {
        return ((float) fahrenheitTemp - 32) * 5.0f / 9.0f;
    }


}
