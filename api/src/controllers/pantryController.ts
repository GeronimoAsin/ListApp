export default new PantryController();

}
  };
    }
      return res.status(500).json({ message: "Internal Server Error" });
    } catch (err) {
      return res.status(result.status).json(result.body);
      const result = await pantryService.createPantry(req.body);
    try {
  public createPantry = async (req: Request, res: Response): Promise<Response> => {
class PantryController {

const pantryService = new PantryService();

import PantryService from "../services/pantryService";

